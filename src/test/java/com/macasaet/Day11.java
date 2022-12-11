package com.macasaet;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * --- Day 11: Monkey in the Middle ---
 * <a href="https://adventofcode.com/2022/day/11">https://adventofcode.com/2022/day/11</a>
 */
public class Day11 {

    public enum Operator implements BiFunction<BigInteger, BigInteger, BigInteger> {
        ADD {
            public BigInteger apply(BigInteger x, BigInteger y) {
                return x.add(y);
            }
        },
        MULTIPLY {
            public BigInteger apply(BigInteger x, BigInteger y) {
                return x.multiply(y);
            }
        };

        public static Operator parse(final String string) {
            return switch(string) {
                case "*" -> MULTIPLY;
                case "+" -> ADD;
                default -> throw new IllegalArgumentException("Invalid operator: " + string);
            };
        }
    }

    public record Operation(Operator operator,
                            Function<BigInteger, BigInteger> lValueSupplier,
                            Function<BigInteger, BigInteger> rValueSupplier) implements Function<BigInteger, BigInteger> {

        public BigInteger apply(final BigInteger oldValue) {
            Objects.requireNonNull(oldValue);
            final var lValue = lValueSupplier.apply(oldValue);
            final var rValue = rValueSupplier.apply(oldValue);
            return operator.apply(lValue, rValue);
        }

        public static Operation parse(String line) {
            line = line.strip();
            if(!line.trim().startsWith("Operation:")) {
                throw new IllegalArgumentException("Not an operation: " + line);
            }
            final var components = line.split(" ");
            final var lValueExpression = components[3];
            final Function<BigInteger, BigInteger> lValueSupplier = "old".equalsIgnoreCase(lValueExpression)
                    ? old -> old
                    : ignored -> new BigInteger(lValueExpression);
            final var operator = Operator.parse(components[4]);
            final var rValueExpression = components[5];
            final Function<BigInteger, BigInteger> rValueSupplier = "old".equalsIgnoreCase(rValueExpression)
                    ? old -> old
                    : ignored -> new BigInteger(rValueExpression);
            return new Operation(operator, lValueSupplier, rValueSupplier);
        }
    }

    /**
     * An observation of how a single monkey behaves
     *
     * @param id the monkey's unique identifier
     * @param items your worry level for each belonging currently held by this monkey
     * @param operation a function describing how your worry level changes when the monkey inspects the item
     * @param divisor used by the monkey to evaluate your worry level and decide what to do with the item
     * @param targetIfTrue the ID of the monkey who will receive the item should the test pass
     * @param targetIfFalse the ID of the monkey who will receive the item should the test fail
     * @param itemsInspected the total number of times this monkey has inspected an item
     */
    public record Monkey(int id, List<BigInteger> items, Operation operation, BigInteger divisor, int targetIfTrue, int targetIfFalse, AtomicReference<BigInteger> itemsInspected) {
        public static Monkey parse(final String block) {
            final var lines = block.split("\n");
            final var id = Integer.parseInt(lines[0].replaceAll("[^0-9]", ""));
            final var startingItems = Arrays.stream(lines[1].strip()
                    .replaceAll("^Starting items: ", "")
                    .split(", "))
                    .map(item -> new BigInteger(item))
                    .collect(Collectors.toList()); // must be mutable
            final var operation = Operation.parse(lines[2]);
            final var divisor = new BigInteger(lines[3].replaceAll("[^0-9]", ""));
            final var targetIfTrue = Integer.parseInt(lines[4].replaceAll("[^0-9]", ""));
            final var targetIfFalse = Integer.parseInt(lines[5].replaceAll("[^0-9]", ""));
            return new Monkey(id, startingItems, operation, divisor, targetIfTrue, targetIfFalse, new AtomicReference<>(BigInteger.ZERO));
        }

        public BigInteger countItemsInspected() {
            return itemsInspected.get();
        }

        public Throw inspectItem(BigInteger reliefFactor) {
            // this assumes monkeys can throw items to themselves
            if(items.isEmpty()) {
                return null;
            }
            var worryLevel = items().remove(0);
            worryLevel = operation().apply(worryLevel);
            worryLevel = worryLevel.divide(reliefFactor);
            final var target = worryLevel.mod(divisor()).equals(BigInteger.ZERO)
                    ? targetIfTrue()
                    : targetIfFalse();
            itemsInspected().updateAndGet(old -> old.add(BigInteger.ONE));
            return new Throw(target, worryLevel);
        }

        public List<Throw> inspectItems(Function<BigInteger, BigInteger> worryUpdater) {
            // this assumes monkeys cannot throw items to themselves
            final var result = items().stream().map(worryLevel -> {
                worryLevel = operation().apply(worryLevel);
                worryLevel = worryUpdater.apply(worryLevel);
                final var target = worryLevel.mod(divisor()).equals(BigInteger.ZERO)
                        ? targetIfTrue()
                        : targetIfFalse();
                return new Throw(target, worryLevel);
            }).toList();
            itemsInspected().updateAndGet(old -> old.add(BigInteger.valueOf(result.size())));
            items().clear();
            return result;
        }

    }

    public record Throw(int target, BigInteger itemWorryLevel) {
    }

    protected List<Monkey> getInput() {
        final var input = StreamSupport
                .stream(new LineSpliterator("day-11.txt"),
                        false).collect(Collectors.joining("\n"));
        return Arrays.stream(input.split("\n\n"))
                .map(Monkey::parse)
                .toList();
    }

    @Test
    public final void part1() {
        final var monkeys = getInput();
        final Function<BigInteger, BigInteger> worryUpdater = worryLevel -> worryLevel.divide(BigInteger.valueOf(3));
        for(int i = 20; --i >= 0; ) {
            for(final var monkey : monkeys) {
                for(final var toss : monkey.inspectItems(worryUpdater)) {
                    monkeys.get(toss.target()).items().add(toss.itemWorryLevel());
                }
            }
        }
        final var result = monkeys.stream()
                .map(Monkey::countItemsInspected)
                .sorted(Comparator.reverseOrder())
                .limit(2)
                .reduce(BigInteger::multiply)
                .get();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var monkeys = getInput();
        final var productOfDivisors = monkeys.stream().map(Monkey::divisor).reduce(BigInteger::multiply).get();
        final Function<BigInteger, BigInteger> worryUpdater = worryLevel -> worryLevel.mod(productOfDivisors);
        for(int i = 10_000; --i >= 0; ) {
            for(final var monkey : monkeys) {
                for(final var toss : monkey.inspectItems(worryUpdater)) {
                    monkeys.get(toss.target()).items().add(toss.itemWorryLevel());
                }
            }
        }
        final var result = monkeys.stream()
                .map(Monkey::countItemsInspected)
                .sorted(Comparator.reverseOrder())
                .limit(2)
                .reduce(BigInteger::multiply)
                .get();
        System.out.println("Part 2: " + result);
    }

}
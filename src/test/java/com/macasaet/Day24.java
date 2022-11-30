package com.macasaet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * --- Day 24: Arithmetic Logic Unit ---
 */
public class Day24 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-24.txt"),
                        false);
    }

    public static class ArithmeticLogicUnit {
        public BigInteger getW() {
            return w;
        }

        public void setW(BigInteger w) {
            this.w = w;
        }

        public BigInteger getX() {
            return x;
        }

        public void setX(BigInteger x) {
            this.x = x;
        }

        public BigInteger getY() {
            return y;
        }

        public void setY(BigInteger y) {
            this.y = y;
        }

        public BigInteger getZ() {
            return z;
        }

        public void setZ(BigInteger z) {
            this.z = z;
        }

        private BigInteger w = BigInteger.ZERO, x = BigInteger.ZERO, y = BigInteger.ZERO, z = BigInteger.ZERO;

        public List<? extends Instruction> getInstructions() {
            return instructions;
        }

        public void setInstructions(List<? extends Instruction> instructions) {
            this.instructions = instructions;
        }

        private List<? extends Instruction> instructions = new ArrayList<>();

        public boolean isValid(final String modelNumber) {
            final var iterator = modelNumber.chars().map(Character::getNumericValue).iterator();
            for (final var instruction : getInstructions()) {
                instruction.evaluate(iterator);
            }
            return BigInteger.ZERO.equals(getZ());
        }

        public static ArithmeticLogicUnit parse(final Stream<String> lines) {
            final var result = new ArithmeticLogicUnit();
            final List<? extends Instruction> instructions = lines.map(line -> {
                final var components = line.split(" ");

                final Consumer<BigInteger> resultSetter = switch (components[1]) {
                    case "w" -> result::setW;
                    case "x" -> result::setX;
                    case "y" -> result::setY;
                    case "z" -> result::setZ;
                    default -> throw new IllegalArgumentException("Invalid instruction, invalid l-value: " + line);
                };
                final Supplier<BigInteger> xSupplier = switch (components[1]) {
                    case "w" -> result::getW;
                    case "x" -> result::getX;
                    case "y" -> result::getY;
                    case "z" -> result::getZ;
                    default -> throw new IllegalArgumentException("Invalid instruction, invalid l-value: " + line);
                };
                final Supplier<BigInteger> ySupplier = components.length > 2 ? switch (components[2]) {
                    case "w" -> result::getW;
                    case "x" -> result::getX;
                    case "y" -> result::getY;
                    case "z" -> result::getZ;
                    default -> () -> new BigInteger(components[2]);
                } : () -> {
                    throw new IllegalStateException();
                };
                return switch (components[0]) {
                    case "inp" -> new Input(resultSetter);
                    case "add" -> new Add(resultSetter, xSupplier, ySupplier);
                    case "mul" -> new Multiply(resultSetter, xSupplier, ySupplier);
                    case "div" -> new Divide(resultSetter, xSupplier, ySupplier);
                    case "mod" -> new Modulo(resultSetter, xSupplier, ySupplier);
                    case "eql" -> new Equals(resultSetter, xSupplier, ySupplier);
                    default -> throw new IllegalArgumentException("Invalid instruction: " + line);
                };
            }).toList();
            result.setInstructions(instructions);
            return result;
        }

        public void reset() {
            setW(BigInteger.ZERO);
            setX(BigInteger.ZERO);
            setY(BigInteger.ZERO);
            setZ(BigInteger.ZERO);
        }
    }

    public interface Instruction {
        void evaluate(PrimitiveIterator.OfInt input);
    }

    public record Input(Consumer<BigInteger> setter) implements Instruction {

        public void evaluate(PrimitiveIterator.OfInt input) {
            setter.accept(BigInteger.valueOf(input.nextInt()));
        }
    }

    public record Add(Consumer<BigInteger> resultSetter, Supplier<BigInteger> xSupplier,
                      Supplier<BigInteger> ySupplier) implements Instruction {
        public void evaluate(PrimitiveIterator.OfInt _input) {
            resultSetter.accept(xSupplier.get().add(ySupplier.get()));
        }
    }

    public record Multiply(Consumer<BigInteger> resultSetter, Supplier<BigInteger> xSupplier,
                           Supplier<BigInteger> ySupplier) implements Instruction {
        public void evaluate(PrimitiveIterator.OfInt _input) {
            resultSetter.accept(xSupplier.get().multiply(ySupplier.get()));
        }
    }

    public record Divide(Consumer<BigInteger> resultSetter, Supplier<BigInteger> xSupplier,
                         Supplier<BigInteger> ySupplier) implements Instruction {
        public void evaluate(PrimitiveIterator.OfInt _input) {
            resultSetter.accept(xSupplier.get().divide(ySupplier.get()));
        }
    }

    public record Modulo(Consumer<BigInteger> resultSetter, Supplier<BigInteger> xSupplier,
                         Supplier<BigInteger> ySupplier) implements Instruction {
        public void evaluate(PrimitiveIterator.OfInt _input) {
            resultSetter.accept(xSupplier.get().mod(ySupplier.get()));
        }
    }

    public record Equals(Consumer<BigInteger> resultSetter, Supplier<BigInteger> xSupplier,
                         Supplier<BigInteger> ySupplier) implements Instruction {
        public void evaluate(PrimitiveIterator.OfInt _input) {
            resultSetter.accept(xSupplier.get().equals(ySupplier.get()) ? BigInteger.ONE : BigInteger.ZERO);
        }
    }

    @Nested
    public class ArithmeticLogicUnitTest {
        @Disabled
        @Test
        public void testNegation() {
            // given
            final var input = """
                    inp x
                    mul x -1
                    """;
            final var alu = ArithmeticLogicUnit.parse(input.lines());

            // when
            alu.isValid("7");

            // then
            assertEquals(-7, alu.getX());
        }

        @Disabled
        @Test
        public final void testThreeTimes() {
            // given
            final var input = """
                    inp z
                    inp x
                    mul z 3
                    eql z x
                    """;
            final var alu = ArithmeticLogicUnit.parse(input.lines());

            // when
            alu.isValid("39");

            // then
            assertEquals(1, alu.getZ());
        }

        @Disabled
        @Test
        public final void testBinaryConversion() {
            // given
            final var input = """
                    inp w
                    add z w
                    mod z 2
                    div w 2
                    add y w
                    mod y 2
                    div w 2
                    add x w
                    mod x 2
                    div w 2
                    mod w 2
                    """;
            final var alu = ArithmeticLogicUnit.parse(input.lines());

            // when
            alu.isValid("9");

            // then
            assertEquals(1, alu.getW());
            assertEquals(0, alu.getX());
            assertEquals(0, alu.getY());
            assertEquals(1, alu.getZ());
        }

        @Test
        public final void testIsValid() {
            // given
            final var alu = ArithmeticLogicUnit.parse(getInput());

            // when
            final var result = alu.isValid("13579246899999");

            // then
            System.err.println("z=" + alu.getZ());
        }
    }

    @Disabled
    @Test
    public final void part1() {
        final var monad = getInput().toList();
        final var counter = new AtomicInteger(0);
        final var result = Stream.iterate(new BigInteger("99999999999999"), previous -> previous.subtract(BigInteger.ONE))
                .parallel()
                .filter(candidate -> {
            final int count = counter.updateAndGet(previous -> previous + 1);
            if(count % 10000 == 0) {
                System.err.println("Testing: " + candidate);
            }
            final var alu = ArithmeticLogicUnit.parse(monad.stream());
            return alu.isValid(candidate.toString());
        }).findFirst();
        System.out.println("Part 1: " + result.orElseThrow());
    }

    @Disabled
    @Test
    public final void part2() {

        System.out.println("Part 2: " + null);
    }

}
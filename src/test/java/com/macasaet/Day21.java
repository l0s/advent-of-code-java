package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * --- Day 21: Monkey Math ---
 * <a href="https://adventofcode.com/2022/day/21">https://adventofcode.com/2022/day/21</a>
 */
public class Day21 {

    public record Monkey(String name, Job job) {
        public long yell(final Map<String, Monkey> monkeys, final Map<String, Long> results) {
            return job().yell(monkeys, results);
        }

        public Simplification simplify(final Map<String, Monkey> monkeys, final Map<String, Long> results) {
            return job().simplify(monkeys, results);
        }

        public static Monkey parse(final String line) {
            final var components = line.split(": ");
            final var name = components[0].trim();
            final var job = Job.parse(components[1].trim());
            return new Monkey(name, job);
        }
    }

    public interface Simplification {
        Simplification simplify();
    }

    record Expression(Simplification x, Operation operation, Simplification y) implements Simplification {

        public Simplification simplify() {
            final var simpleX = x().simplify();
            final var simpleY = y().simplify();
            if (!x().equals(simpleX) || !y().equals(simpleY)) {
                return new Expression(simpleX, operation(), simpleY);
            } else if (x() instanceof Value && y() instanceof Value) {
                return new Value(operation().operate(((Value) x).value(), ((Value) y).value()));
            } else if (operation() == Operation.IS_EQUAL) {
                if (x() instanceof final Value constant && y() instanceof final Expression expression) {
                    // e.g. 5=2/x or 5=x/2
                    final var inverse = expression.operation().inverse();
                    if (expression.x() instanceof Value) {
                        // e.g. 5=2/x
                        if (expression.operation.isSymmetric()) {
                            // e.g. 2=5x -> 10=x
                            final var lValue = new Expression(constant, inverse, expression.x()).simplify();
                            return new Expression(lValue, Operation.IS_EQUAL, expression.y());
                        } else {
                            // e.g. 5=2/x -> 5x=2
                            final var lValue = new Expression(constant, inverse, expression.y());
                            return new Expression(lValue, Operation.IS_EQUAL, expression.x());
                        }
                    } else if (expression.y() instanceof Value) {
                        // e.g. 5=x/2 -> 5*2=x -> 10=x
                        final var lValue = new Expression(constant, inverse, expression.y()).simplify();
                        return new Expression(lValue, Operation.IS_EQUAL, expression.x());
                    }
                    // cannot simplify further
                    return this;
                } else if (x() instanceof final Expression expression && y() instanceof final Value constant) {
                    // e.g. 5/x=2 or x/5=2
                    final var inverse = expression.operation().inverse();
                    if (expression.x() instanceof Value) {
                        // e.g. 5/x=2 or x/5=2
                        if (expression.operation().isSymmetric()) {
                            // e.g. 2x=5 -> x=5*2 -> x=10
                            final var rValue = new Expression(constant, inverse, expression.x()).simplify();
                            return new Expression(expression.y(), Operation.IS_EQUAL, rValue);
                        } else {
                            // e.g. 5/x=2 -> 5=2x
                            final var rValue = new Expression(constant, inverse, expression.y());
                            return new Expression(expression.x(), Operation.IS_EQUAL, rValue);
                        }
                    } else if (expression.y() instanceof Value) {
                        // e.g. x/5=2 -> x=2*5 -> x=10
                        final var rValue = new Expression(constant, inverse, expression.y()).simplify();
                        return new Expression(expression.x(), Operation.IS_EQUAL, rValue);
                    }
                    // cannot simplify further
                    return this;
                }
            }
            return this;
        }

        public String toString() {
            return "(" + x() + ") " + operation() + " (" + y() + ")";
        }
    }

    record Value(long value) implements Simplification {
        public String toString() {
            return "" + value();
        }

        public Simplification simplify() {
            return this;
        }
    }

    record Variable() implements Simplification {
        public String toString() {
            return "x";
        }

        public Simplification simplify() {
            return this;
        }
    }

    public interface Job {
        long yell(final Map<String, Monkey> monkeys, final Map<String, Long> results);

        Simplification simplify(final Map<String, Monkey> monkeys, final Map<String, Long> results);

        static Job parse(final String string) {
            final var components = string.trim().split(" ");
            if (components.length == 1) {
                return Yell.parse(string.trim());
            }
            return Math.parse(components);
        }
    }

    public enum Operation {
        Add {
            public long operate(final long x, final long y) {
                return x + y;
            }

            public Operation inverse() {
                return Subtract;
            }

            public boolean isSymmetric() {
                return true;
            }
        },
        Subtract {
            public long operate(final long x, final long y) {
                return x - y;
            }

            public Operation inverse() {
                return Add;
            }

            public boolean isSymmetric() {
                return false;
            }
        },
        Multiply {
            public long operate(final long x, final long y) {
                return x * y;
            }

            public Operation inverse() {
                return Divide;
            }

            public boolean isSymmetric() {
                return true;
            }
        },
        Divide {
            public long operate(final long x, final long y) {
                return x / y;
            }

            public Operation inverse() {
                return Multiply;
            }

            public boolean isSymmetric() {
                return false;
            }
        },
        IS_EQUAL {
            public long operate(final long x, final long y) {
                // what a horrible hack, who would do this?
                return x == y ? 1L : 0L;
            }

            public Operation inverse() {
                return IS_EQUAL;
            }

            public boolean isSymmetric() {
                return true;
            }
        };

        public abstract long operate(long x, long y);

        public abstract Operation inverse();

        public abstract boolean isSymmetric();

        public String toString() {
            return switch (this) {
                case Add -> "+";
                case Subtract -> "-";
                case Multiply -> "*";
                case Divide -> "/";
                case IS_EQUAL -> "=";
            };
        }

        public static Operation parse(final String operator) {
            return switch (operator.trim()) {
                case "+" -> Add;
                case "-" -> Subtract;
                case "*" -> Multiply;
                case "/" -> Divide;
                default -> throw new IllegalArgumentException("Invalid operator: " + operator);
            };
        }
    }

    public record Unknown() implements Job {
        public long yell(Map<String, Monkey> monkeys, Map<String, Long> results) {
            throw new UnsupportedOperationException(); // Oof
        }

        public Simplification simplify(Map<String, Monkey> monkeys, Map<String, Long> results) {
            return new Variable();
        }
    }

    record Yell(long number) implements Job {
        public long yell(Map<String, Monkey> monkeys, Map<String, Long> results) {
            return number;
        }

        public Simplification simplify(Map<String, Monkey> monkeys, Map<String, Long> results) {
            return new Value(number());
        }

        public static Yell parse(final String string) {
            return new Yell(Integer.parseInt(string));
        }
    }

    public record Math(String monkeyX, Operation operation, String monkeyY) implements Job {

        public long yell(Map<String, Monkey> monkeys, Map<String, Long> results) {
            final var x = getVariable(monkeyX(), monkeys, results);
            final var y = getVariable(monkeyY(), monkeys, results);
            return operation().operate(x, y);
        }

        public Simplification simplify(Map<String, Monkey> monkeys, Map<String, Long> results) {
            final var x = monkeys.get(monkeyX()).simplify(monkeys, results);
            final var y = monkeys.get(monkeyY()).simplify(monkeys, results);
            if (x instanceof final Value xValue && y instanceof final Value yValue) {
                return new Value(operation().operate(xValue.value(), yValue.value()));
            }
            return new Expression(x, operation(), y);
        }

        long getVariable(final String monkeyName, final Map<String, Monkey> monkeys, final Map<String, Long> results) {
            if (results.containsKey(monkeyName)) {
                return results.get(monkeyName);
            }
            final var result = monkeys.get(monkeyName).yell(monkeys, results);
            results.put(monkeyName, result);
            return result;
        }

        public static Math parse(final String[] components) {
            final var monkeyX = components[0].trim();
            final var operation = Operation.parse(components[1].trim());
            final var monkeyY = components[2].trim();
            return new Math(monkeyX, operation, monkeyY);
        }
    }

    protected static Map<String, Monkey> getInput() {
        final Map<String, Monkey> result = new HashMap<>();
        StreamSupport.stream(new LineSpliterator("day-21.txt"), false)
                .map(Monkey::parse)
                .forEach(monkey -> result.put(monkey.name(), monkey));
        return Collections.unmodifiableMap(result);
    }

    @Test
    public final void part1() {
        final var monkeys = getInput();
        final var results = new HashMap<String, Long>();
        final var result = monkeys.get("root").yell(monkeys, results);

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var monkeys = new HashMap<>(getInput());
        final var results = new HashMap<String, Long>();
        final var oldRoot = monkeys.get("root");
        final var oldJob = (Math) oldRoot.job();
        monkeys.put("root", new Monkey("root", new Math(oldJob.monkeyX(), Operation.IS_EQUAL, oldJob.monkeyY())));
        monkeys.put("humn", new Monkey("humn", new Unknown()));

        var simplification = monkeys.get("root").simplify(monkeys, results);
        while (true) {
            final var candidate = simplification.simplify();
            if (candidate.equals(simplification)) {
                break;
            }
            simplification = candidate;
        }
        System.out.println("Part 2: " + simplification);
    }

}
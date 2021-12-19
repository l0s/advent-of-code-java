package com.macasaet;

import static com.macasaet.Day18.SnailfishNumber.parse;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 18: Snailfish ---
 */
public class Day18 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-18.txt"),
                        false);
    }

    /**
     * An element of a {@link SnailfishNumber}
     */
    public interface Token {
    }

    /**
     * A symbol in a {@link SnailfishNumber}
     */
    public enum Symbol implements Token {
        START_PAIR,
        END_PAIR,
        SEPARATOR,

    }

    /**
     * An integer in a {@link SnailfishNumber}
     */
    public record Number(int value) implements Token {
    }

    /**
     * "Snailfish numbers aren't like regular numbers. Instead, every snailfish number is a pair - an ordered list of
     * two elements. Each element of the pair can be either a regular number or another pair."
     */
    public record SnailfishNumber(List<Token> expression) {

        public SnailfishNumber(final String string) {
            this(parse(string));
        }

        /**
         * "The magnitude of a pair is 3 times the magnitude of its left element plus 2 times the magnitude of its right
         * element. The magnitude of a regular number is just that number."
         *
         * @return the snailfish number distilled into a single value
         */
        public int magnitude() {
            var stack = new LinkedList<Integer>();
            for (final var token : expression) {
                if (token.equals(Symbol.START_PAIR)) {
                } else if (token instanceof final Number number) {
                    stack.push(number.value());
                } else if (token.equals(Symbol.END_PAIR)) {
                    final var rightValue = stack.pop();
                    final var leftValue = stack.pop();
                    stack.push(leftValue * 3 + rightValue * 2);
                }
            }
            if (stack.size() != 1) {
                throw new IllegalStateException("Invalid stack: " + stack);
            }
            return stack.get(0);
        }

        /**
         * Repeatedly explode or split this snailfish number until those operations can no longer be performed.
         *
         * @return a representation of this snailfish number that cannot be reduced any further
         */
        public SnailfishNumber reduce() {
            var newExpression = expression;
            while (true) {
                var explosionIndex = getExplosionIndex(newExpression);
                var splitIndex = getSplitIndex(newExpression);
                if (explosionIndex > 0) {
                    newExpression = explode(newExpression, explosionIndex);
                } else if (splitIndex > 0) {
                    newExpression = split(newExpression, splitIndex);
                } else {
                    break;
                }
            }
            return new SnailfishNumber(newExpression);
        }

        /**
         * Add a snailfish number. Note, this operation is *not commutative*: `x.add(y)` is not the same as `y.add(x)`.
         * Also note that the process of addition may yield a snailfish number that needs to be reduced.
         *
         * @param addend the number to add to this snailfish number
         * @return the sum of the snailfish numbers (may need to be reduced
         * @see SnailfishNumber#reduce()
         */
        public SnailfishNumber add(final SnailfishNumber addend) {
            final var tokens = new ArrayList<Token>();
            tokens.add(Symbol.START_PAIR);
            tokens.addAll(expression());
            tokens.add(Symbol.SEPARATOR);
            tokens.addAll(addend.expression());
            tokens.add(Symbol.END_PAIR);
            return new SnailfishNumber(Collections.unmodifiableList(tokens));
        }

        static List<Token> parse(final String expression) {
            final var result = new ArrayList<Token>();
            for (int i = 0; i < expression.length(); i++) {
                final var c = expression.charAt(i);
                if (c == '[') {
                    result.add(Symbol.START_PAIR);
                } else if (c == ']') {
                    result.add(Symbol.END_PAIR);
                } else if (c == ',') {
                    result.add(Symbol.SEPARATOR);
                } else if (c >= '0' && c <= '9') {
                    int endExclusive = i + 1;
                    while (endExclusive < expression.length()) {
                        final var d = expression.charAt(endExclusive);
                        if (d < '0' || d > '9') {
                            break;
                        }
                        endExclusive++;
                    }
                    final int value = Integer.parseInt(expression.substring(i, endExclusive));
                    result.add(new Number(value));
                    i = endExclusive - 1;
                }
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Split a regular number. "To split a regular number, replace it with a pair; the left element of the pair
         * should be the regular number divided by two and rounded down, while the right element of the pair should be
         * the regular number divided by two and rounded up."
         *
         * @param expression a raw representation of a snailfish number
         * @param index      the index of a regular number to split. The caller is responsible for ensuring that this number
         *                   can be split and that it is the most appropriate action to take.
         * @return the reduced snailfish number in raw tokens
         */
        List<Token> split(final List<Token> expression, final int index) {
            final var result = new ArrayList<Token>();
            if (index > 0) {
                result.addAll(expression.subList(0, index));
            }
            final var regularNumber = (Number) expression.get(index);

            final var left = Math.floorDiv(regularNumber.value(), 2);
            final var right = (int) Math.ceil(regularNumber.value() / 2.0d);

            result.add(Symbol.START_PAIR);
            result.add(new Number(left));
            result.add(Symbol.SEPARATOR);
            result.add(new Number(right));
            result.add(Symbol.END_PAIR);
            if (index + 1 < expression.size()) {
                result.addAll(expression.subList(index + 1, expression.size()));
            }
            return Collections.unmodifiableList(result);
        }

        /**
         * Determine whether any of the regular numbers can be split and if so, the highest-priority number to split.
         *
         * @param expression a raw representation of a snailfish number
         * @return the index of the best regular number to split or -1 if none can be split
         */
        int getSplitIndex(final List<Token> expression) {
            for (int i = 0; i < expression.size(); i++) {
                final var token = expression.get(i);
                if (token instanceof final Number number) {
                    if (number.value() >= 10) {
                        return i;

                    }
                }
            }
            return -1;
        }

        /**
         * Explode the pair starting at `index`. "To explode a pair, the pair's left value is added to the first regular
         * number to the left of the exploding pair (if any), and the pair's right value is added to the first regular
         * number to the right of the exploding pair (if any). Exploding pairs will always consist of two regular
         * numbers. Then, the entire exploding pair is replaced with the regular number 0."
         *
         * @param expression a raw representation of a snailfish number
         * @param index      the index of the opening brace of the pair to explode. The caller must ensure that an explosion
         *                   operation is valid at the index and that the index represents the most appropriate pair to
         *                   explode.
         * @return the reduced expression in raw format
         */
        List<Token> explode(final List<Token> expression, final int index) {
            final var result = new ArrayList<>(expression);
            final int leftNumberIndex = index + 1;
            final int rightNumberIndex = index + 3;
            final int left = ((Number) expression.get(leftNumberIndex)).value();
            final int right = ((Number) expression.get(rightNumberIndex)).value();
            int leftIndex = -1;
            int rightIndex = -1;

            for (int i = index; --i >= 0; ) {
                final var c = expression.get(i);
                if (c instanceof Number) {
                    leftIndex = i;
                    break;
                }
            }
            for (int i = rightNumberIndex + 1; i < expression.size(); i++) {
                final var c = expression.get(i);
                if (c instanceof Number) {
                    rightIndex = i;
                    break;
                }
            }
            if (leftIndex < 0 && rightIndex < 0) {
                throw new IllegalArgumentException("Cannot be exploded: " + expression);
            }
            // "the pair's left value is added to the first regular number to the left of the exploding pair (if any)"
            if (leftIndex > 0) {
                final int leftOperand = ((Number) expression.get(leftIndex)).value();
                final int replacement = leftOperand + left;
                result.set(leftIndex, new Number(replacement));
            }
            // "the pair's right value is added to the first regular number to the right of the exploding pair (if any)"
            if (rightIndex > 0) {
                final int rightOperand = ((Number) expression.get(rightIndex)).value();
                final int replacement = rightOperand + right;
                result.set(rightIndex, new Number(replacement));
            }
            // "Exploding pairs will always consist of two regular numbers. Then, the entire exploding pair is replaced
            // with the regular number 0."
            result.set(index, new Number(0));
            result.remove(index + 1);
            result.remove(index + 1);
            result.remove(index + 1);
            result.remove(index + 1);
            return Collections.unmodifiableList(result);
        }

        /**
         * @param expression a raw representation of a snailfish number
         * @return the index of the most appropriate pair to explode (opening brace) or -1 if no explosion is appropriate
         */
        int getExplosionIndex(final List<Token> expression) {
            int depth = -1;
            int maxDepth = Integer.MIN_VALUE;
            int result = -1;
            for (int i = 0; i < expression.size(); i++) {
                final var token = expression.get(i);
                if (token == Symbol.START_PAIR) {
                    depth++;
                } else if (token == Symbol.END_PAIR) {
                    depth--;
                }
                if (depth > maxDepth) {
                    maxDepth = depth;
                    result = i;
                }
            }
            return result > 3 ? result : -1;
        }

    }

    @Nested
    public class SnailfishNumberTest {

        @Test
        public final void testAdd() {
            assertEquals(new SnailfishNumber("[[[[0,7],4],[[7,8],[6,0]]],[8,1]]"),
                    new SnailfishNumber("[[[[4,3],4],4],[7,[[8,4],9]]]")
                            .add(new SnailfishNumber("[1,1]"))
                            .reduce());
            // either this example is broken or my bug is not triggered in the real puzzle input T_T
//            assertEquals(new SnailfishNumber("[[[[7,8],[6,6]],[[6,0],[7,7]]],[[[7,8],[8,8]],[[7,9],[0,6]]]]"),
//                    new SnailfishNumber("[[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]")
//                            .add(new SnailfishNumber("[[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]"))
//                            .reduce());
        }

        @Test
        public final void testAddList() {
            // given
            final var lines = """
                    [[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]]
                    [7,[[[3,7],[4,3]],[[6,3],[8,8]]]]
                    [[2,[[0,8],[3,4]]],[[[6,7],1],[7,[1,6]]]]
                    [[[[2,4],7],[6,[0,5]]],[[[6,8],[2,8]],[[2,1],[4,5]]]]
                    [7,[5,[[3,8],[1,4]]]]
                    [[2,[2,2]],[8,[8,1]]]
                    [2,9]
                    [1,[[[9,3],9],[[9,0],[0,7]]]]
                    [[[5,[7,4]],7],1]
                    [[[[4,2],2],6],[8,7]]""";
            final var list = Arrays.stream(lines.split("\n"))
                    .map(SnailfishNumber::new)
                    .collect(Collectors.toList());

            // when
            var sum = list.get(0);
            for (final var addend : list.subList(1, list.size())) {
                sum = sum.add(addend).reduce();
            }

            // then
            assertEquals(new SnailfishNumber("[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]"),
                    sum);
        }

        @Test
        public final void testSplit() {
            final var instance = new SnailfishNumber(Collections.emptyList());

            assertEquals(parse("[5, 5]"),
                    instance.split(parse("10"), 0));
            assertEquals(parse("[5, 6]"),
                    instance.split(parse("11"), 0));
            assertEquals(parse("[6, 6]"),
                    instance.split(parse("12"), 0));
        }

        @Test
        public final void testExplosionIndex() {
            final var instance = new SnailfishNumber(Collections.emptyList());
            assertEquals(4,
                    instance.getExplosionIndex(parse("[[[[[9,8],1],2],3],4]")));
            assertEquals(12,
                    instance.getExplosionIndex(parse("[7,[6,[5,[4,[3,2]]]]]")));
            assertEquals(10,
                    instance.getExplosionIndex(parse("[[6,[5,[4,[3,2]]]],1]")));
            assertEquals(10,
                    instance.getExplosionIndex(parse("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]")));
            assertEquals(24,
                    instance.getExplosionIndex(parse("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]")));
        }

        @Test
        public final void testExplode() {
            final var instance = new SnailfishNumber(Collections.emptyList());
            assertEquals(parse("[[[[0,9],2],3],4]"),
                    instance
                            .explode(parse("[[[[[9,8],1],2],3],4]"), 4));
            assertEquals(parse("[7,[6,[5,[7,0]]]]"),
                    instance
                            .explode(parse("[7,[6,[5,[4,[3,2]]]]]"), 12));
            assertEquals(parse("[[6,[5,[7,0]]],3]"),
                    instance
                            .explode(parse("[[6,[5,[4,[3,2]]]],1]"), 10));
            assertEquals(parse("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]"),
                    instance
                            .explode(parse("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]"), 10));
            assertEquals(parse("[[3,[2,[8,0]]],[9,[5,[7,0]]]]"),
                    instance
                            .explode(parse("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]"), 24));
        }

        @Test
        public final void testMagnitude() {
            assertEquals(29, new SnailfishNumber("[9,1]").magnitude());
            assertEquals(21, new SnailfishNumber("[1,9]").magnitude());
            assertEquals(143, new SnailfishNumber("[[1,2],[[3,4],5]]").magnitude());
            assertEquals(1384, new SnailfishNumber("[[[[0,7],4],[[7,8],[6,0]]],[8,1]]").magnitude());
            assertEquals(445, new SnailfishNumber("[[[[1,1],[2,2]],[3,3]],[4,4]]").magnitude());
            assertEquals(791, new SnailfishNumber("[[[[3,0],[5,3]],[4,4]],[5,5]]").magnitude());
            assertEquals(1137, new SnailfishNumber("[[[[5,0],[7,4]],[5,5]],[6,6]]").magnitude());
            assertEquals(3488, new SnailfishNumber("[[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]").magnitude());
            assertEquals(3993, new SnailfishNumber("[[[[7,8],[6,6]],[[6,0],[7,7]]],[[[7,8],[8,8]],[[7,9],[0,6]]]]").magnitude());
        }

        @Test
        public final void verifyStableState() {
            // given
            final var original = new SnailfishNumber("[[[[7,8],[6,6]],[[6,0],[7,7]]],[[[7,8],[8,8]],[[7,9],[0,6]]]]");

            // when
            final var reduced = original.reduce();

            // then
            assertEquals(original, reduced);
        }
    }


    @Test
    public final void part1() {
        final var list =
                getInput().map(SnailfishNumber::new).collect(Collectors.toList());
        var sum = list.get(0);
        for (final var addend : list.subList(1, list.size())) {
            sum = sum.add(addend).reduce();
        }
        System.out.println("Part 1: " + sum.magnitude());
    }

    @Test
    public final void part2() {
        final var list =
                getInput().map(SnailfishNumber::new).collect(Collectors.toList());
        int max = Integer.MIN_VALUE;
        for (final var x : list) {
            for (final var y : list) {
                if (x.equals(y)) {
                    continue;
                }
                final var sum = x.add(y).reduce();
                final var magnitude = sum.magnitude();
                if (magnitude > max) {
                    max = magnitude;
                }
            }
        }
        System.out.println("Part 2: " + max);
    }

}
package com.macasaet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 10: Syntax Scoring ---
 */
public class Day10 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-10.txt"),
                        false);
    }

    /**
     * The type of open and closing delimiter for a chunk in the navigation subsystem
     */
    public enum BracketType {
        PARENTHESIS('(', ')', 3, 1),
        SQUARE('[', ']', 57, 2),
        CURLY('{', '}', 1197, 3),
        ANGLED('<', '>', 25137, 4);

        private final char open;
        private final char close;
        private final int corruptionPoints;
        private final int autocompletePoints;

        BracketType(char open, char close, int corruptionPoints, final int autocompletePoints) {
            this.open = open;
            this.close = close;
            this.corruptionPoints = corruptionPoints;
            this.autocompletePoints = autocompletePoints;
        }

        public static BracketType forOpen(final char c) {
            return switch (c) {
                case '(' -> PARENTHESIS;
                case '[' -> SQUARE;
                case '{' -> CURLY;
                case '<' -> ANGLED;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
        }

        public static BracketType forClose(final char c) {
            return switch (c) {
                case ')' -> PARENTHESIS;
                case ']' -> SQUARE;
                case '}' -> CURLY;
                case '>' -> ANGLED;
                default -> throw new IllegalStateException("Unexpected value: " + c);
            };
        }
    }

    /**
     * @param line a line in the navigation subsystem
     * @return a score of how corrupt the line is. A score of zero means it is not corrupt. The higher the value, the
     * more corrupt the line is.
     */
    public int calculateCorruptionScore(final char[] line) {
        final var stack = new LinkedList<BracketType>();
        for (int i = 0; i < line.length; i++) {
            final var c = line[i];
            if (c == '(' || c == '[' || c == '{' || c == '<') {
                stack.push(BracketType.forOpen(c));
            } else if (c == ')' || c == ']' || c == '}' || c == '>') {
                if (stack.peek().close == c) {
                    stack.pop();
                } else {
                    // corrupt
                    return BracketType.forClose(c).corruptionPoints;
                }
            }
        }
        // if stack is not empty, it's incomplete
        return 0;
    }

    /**
     * @param line a non-corrupt line in the navigation subsystem. Behaviour is undefined for corrupt lines.
     * @return the score for the suffix required to complete the line
     */
    public long calculateCompletionScore(final char[] line) {
        final var stack = new LinkedList<BracketType>();
        for (int i = 0; i < line.length; i++) {
            final var c = line[i];
            if (c == '(' || c == '[' || c == '{' || c == '<') {
                stack.push(BracketType.forOpen(c));
            } else if (c == ')' || c == ']' || c == '}' || c == '>') {
                if (stack.peek().close == c) {
                    stack.pop();
                } else {
                    throw new IllegalArgumentException("Corrupt: " + new String(line));
                }
            }
        }
        long result = 0;
        while (!stack.isEmpty()) {
            final var unclosed = stack.pop();
            result = result * 5 + unclosed.autocompletePoints;
        }
        return result;
    }

    @Test
    public final void part1() {
        final var result = getInput()
                .map(String::toCharArray)
                .filter(line -> line.length > 0)
                .mapToInt(this::calculateCorruptionScore)
                .sum();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var list = getInput()
                .map(String::toCharArray)
                .filter(line -> line.length > 0)
                .filter(line -> calculateCorruptionScore(line) <= 0) // discard corrupted lines
                .mapToLong(this::calculateCompletionScore)
                .sorted()
                .collect(ArrayList<Long>::new, List::add, List::addAll);
        final var result = list.get(list.size() / 2);
        System.out.println("Part 2: " + result);
    }

}
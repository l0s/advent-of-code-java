package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 13: Distress Signal ---
 * <a href="https://adventofcode.com/2022/day/13">https://adventofcode.com/2022/day/13</a>
 */
public class Day13 {

    public record Pair(ListItem x, ListItem y) {
        static Pair parse(final String lines) {
            final var components = lines.split("\n");
            final var x = ListItem.parse(components[0]);
            final var y = ListItem.parse(components[1]);
            return new Pair(x, y);
        }

        public boolean isInOrder() {
            return x().compareTo(y()) < 0;
        }

        public Stream<ListItem> stream() {
            return Stream.of(x(), y()).sorted();
        }
    }

    interface Item extends Comparable<Item> {

        int compareToList(ListItem other);
        int compareToLiteral(Literal other);

        default int compareTo(Item other) {
            if(other instanceof ListItem) {
                return compareToList((ListItem)other);
            } else {
                if(!(other instanceof Literal)) {
                    throw new IllegalArgumentException("Unknown implementation");
                }
                return compareToLiteral((Literal) other);
            }
        }
    }

    public record ListItem(List<Item> items) implements Item {
        public static ListItem parse(final String string) {
            final var stack = new ArrayDeque<ListItem>();
            StringBuilder numberBuffer = new StringBuilder();
            for(final char c : string.toCharArray()) {
                if(c == '[') {
                    stack.push(new ListItem(new ArrayList<>()));
                } else if(c == ']') {
                    if(!numberBuffer.isEmpty()) {
                        final var numberString = numberBuffer.toString();
                        numberBuffer.delete(0, numberBuffer.length());
                        final var number = Integer.parseInt(numberString);
                        stack.peek().items().add(new Literal(number));
                    }
                    if(stack.size() > 1) {
                        final var completed = stack.pop();
                        stack.peek().items().add(completed);
                    }
                } else if(c == ',') {
                    if(!numberBuffer.isEmpty()) {
                        final var numberString = numberBuffer.toString();
                        numberBuffer.delete(0, numberBuffer.length());
                        final var number = Integer.parseInt(numberString);
                        stack.peek().items().add(new Literal(number));
                    }
                } else {
                    numberBuffer.append(c);
                }
            }
            return stack.pop();
        }

        public int compareToList(ListItem other) {
            final Iterator<Item> x = this.items().iterator();
            final Iterator<Item> y = other.items().iterator();
            while(x.hasNext() && y.hasNext()) {
                final var xItem = x.next();
                final var yItem = y.next();
                final var comparison = xItem.compareTo(yItem);
                if(comparison != 0) {
                    return comparison;
                }
            }
            if(y.hasNext()) {
                return -1;
            } else if(x.hasNext()) {
                return 1;
            }
            return 0;
        }

        public int compareToLiteral(Literal other) {
            return compareToList(other.asList());
        }

    }

    public record Literal(int item) implements Item {
        public int compareToList(ListItem other) {
            return asList().compareToList(other);
        }

        public int compareToLiteral(Literal other) {
            return Integer.compare(item(), other.item());
        }

        public ListItem asList() {
            return new ListItem(Collections.singletonList(this));
        }
    }

    protected List<Pair> getInput() {
        final var lines = StreamSupport.stream(new LineSpliterator("day-13.txt"), false)
                .collect(Collectors.joining("\n"));
        final var blocks = lines.split("\n\n");
        return Arrays.stream(blocks).map(block -> Pair.parse(block)).toList();
    }

    @Test
    public final void part1() {
        final var pairs = getInput();
        var result = 0;
        for(int i = 0; i < pairs.size(); i++) {
            final var pair = pairs.get(i);
            if(pair.isInOrder()) {
                result += i + 1;
            }
        }
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var pairs = getInput();
        final var packets = pairs.stream().flatMap(Pair::stream).sorted().toList();
        final int leftSearchResult = Collections.binarySearch(packets,
                new ListItem(Collections.singletonList(new ListItem(Collections.singletonList(new Literal(2))))));
        final int leftInsertionPoint = -(leftSearchResult + 1) + 1;
        final int rightSearchResult = Collections.binarySearch(packets,
                new ListItem(Collections.singletonList(new ListItem(Collections.singletonList(new Literal(6))))));
        final int rightInsertionPoint = -(rightSearchResult + 1) + 2;
        final int result = leftInsertionPoint * rightInsertionPoint;

        System.out.println("Part 2: " + result);
    }

}
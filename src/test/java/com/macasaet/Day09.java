package com.macasaet;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day09 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day09.class.getResourceAsStream("/day-9-input.txt"))) {
            final int preambleSize = 25;
            final var list = StreamSupport.stream(spliterator, false)
                    .map(Long::parseLong)
                    .collect(Collectors.toUnmodifiableList());
            long invalid = Long.MIN_VALUE;
            outer: for (int i = preambleSize; i < list.size(); i++) {
                final var current = list.get(i);
                final var valid = list
                        .subList(i - preambleSize, i)
                        .stream()
                        .filter(l -> l <= current) // no negative numbers in the input, so filter out anything larger than our target
                        .collect(Collectors.toUnmodifiableList());

                for (int j = valid.size(); --j >= 1; ) {
                    final var x = valid.get(j);
                    for (int k = j; --k >= 0; ) {
                        final var y = valid.get(k);
                        if (x + y == current) {
                            continue outer;
                        }
                    }
                }
                invalid = current;
                System.out.println("Part 1: " + invalid);
                break;
            }
            outer: for (int i = list.size(); --i >= 1;) {
                var total = list.get(i);
                var min = total;
                var max = total;
                for (int j = i; --j >= 0;) {
                    final var current = list.get(j);
                    if( current < min ) min = current;
                    if( current > max ) max = current;
                    total += current;
                    if (total == invalid) {
                        final var result = min + max;
                        System.out.println("Part 2: " + result);
                        return;
                    } else if (total > invalid) { // I can only do this because there are no negative numbers
                        continue outer;
                    }
                }
            }
        }
    }
}
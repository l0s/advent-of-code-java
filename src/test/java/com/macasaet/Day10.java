package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day10 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day10.class.getResourceAsStream("/day-10-input.txt"))) {
            final var adapterJoltages = StreamSupport.stream(spliterator, false)
                    .map(Integer::parseInt)
                    .sorted() // ensure the next adapter in the chain is always to the right
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());
            final var targetJoltage = adapterJoltages.get(adapterJoltages.size() - 1) + 3;

            int current = 0;
            var distribution = new int[]{0, 0, 1}; // the last chain link has a difference of 3
            while (current < targetJoltage - 3) {
                final var _current = current;
                final var next = adapterJoltages
                        .stream()
                        .filter(candidate -> candidate - _current >= 1 && candidate - _current <= 3)
                        .findFirst()
                        .get();
                final var difference = next - current;
                distribution[difference - 1]++;
                current = next;
            }
            System.out.println("Part 1: " + (distribution[0] * distribution[2]));
            System.out.println("Part 2: " + count(adapterJoltages, 0, targetJoltage));
        }
    }

    private static final Map<Integer, Long> cache = new HashMap<>();

    protected static int hash(final Collection<Integer> adapters, final int from, final int to) {
        int retval = 0;
        retval = 31 * retval + from;
        retval = 31 * retval + to;
        retval = 31 * retval + adapters.hashCode();
        return retval;
    }

    protected static long count(final List<Integer> adapters, final int from, final int to) {
        final var hash = hash(adapters, from, to);
        if (cache.containsKey(hash)) {
            return cache.get(hash);
        }

        if (adapters.isEmpty()) {
            return from + 3 == to ? 1 : 0;
        } else if (adapters.size() == 1) {
            final int single = adapters.get(0);
            return single == to - 3 ? 1 : 0;
        }

        long retval = 0;
        for (int i = 3; --i >= 0; ) {
            if (i >= adapters.size()) continue;
            final int first = adapters.get(i);
            final int difference = first - from;
            if (difference >= 1 && difference <= 3) {
                final var remaining =
                        i < adapters.size()
                        ? adapters.subList(i + 1, adapters.size())
                        : new ArrayList<Integer>();
                retval += count(remaining, first, to);
            }
        }
        cache.put(hash, retval);
        return retval;
    }
}
package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class Day16 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day16.class.getResourceAsStream("/day-16-input.txt"))) {
            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());

            final var fields = new HashSet<Field>();
            int section = 0;
            Ticket myTicket = null;
            final var nearbyTickets = new ArrayList<Ticket>();
            for (final var line : lines) {
                if (line.isBlank()) {
                    section++;
                    continue;
                } else if ("your ticket:".equalsIgnoreCase(line.strip()) || "nearby tickets:".equalsIgnoreCase(line.strip())) {
                    continue;
                }
                switch (section) {
                    case 0 -> {
                        final var sections = line.split(": ");
                        final var label = sections[0].strip().replaceAll(":$", "");
                        final var rangesString = sections[1].strip();
                        final var rangeStringArray = rangesString.split(" or ");
                        fields.add(new Field(label,
                                Arrays.stream(rangeStringArray)
                                        .map(Range::fromString)
                                        .sorted()
                                        .collect(Collectors.toUnmodifiableList())));
                    }
                    case 1 -> myTicket = new Ticket(Arrays.stream(line.split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toUnmodifiableList()));
                    case 2 -> nearbyTickets.add(new Ticket(Arrays.stream(line.split(","))
                            .map(Integer::parseInt)
                            .collect(Collectors.toUnmodifiableList())));
                }
            }

            final int sum = nearbyTickets
                    .stream()
                    .flatMapToInt(ticket -> ticket
                            .getInvalidNumbers(fields)
                            .stream()
                            .mapToInt(Integer::intValue))
                    .sum();
            System.out.println("Part 1: " + sum);

            final var validTickets = nearbyTickets
                    .stream()
                    .filter(candidate -> candidate.isValid(fields))
                    .collect(Collectors.toUnmodifiableSet());
            final var unmappedIndices = IntStream
                    .range(0, myTicket.numbers.size())
                    .collect(HashSet<Integer>::new, Set::add, Set::addAll);
            final var fieldTable = new HashMap<String, Integer>();
            final var unmappedFields = new HashSet<>(fields);
            while (!unmappedFields.isEmpty()) {
                final var indicesToRemove = new HashSet<Integer>();
                for (final int index : unmappedIndices) {
                    final var candidates = new HashSet<>(unmappedFields);
                    final var toRemove = new HashSet<Field>();
                    for (final var ticket : validTickets) {
                        final var number = ticket.numbers.get(index);
                        for (final var candidate : candidates) {
                            if (!candidate.contains(number)) {
                                toRemove.add(candidate);
                            }
                        }
                        candidates.removeAll(toRemove);
                    }
                    if (candidates.isEmpty()) {
                        throw new IllegalStateException("no candidates for index: " + index);
                    } else if (candidates.size() == 1) {
                        // map candidate to index
                        final var field = candidates.iterator().next();
                        fieldTable.put(field.label, index);
                        unmappedFields.remove(field);
                        indicesToRemove.add(index);
                    }
                }
                unmappedIndices.removeAll(indicesToRemove);
            }
            final var numbers = myTicket.numbers;
            final long product = fieldTable
                    .keySet()
                    .stream()
                    .filter(candidate -> candidate.startsWith("departure"))
                    .map(fieldTable::get)
                    .map(numbers::get)
                    .mapToLong(Long::valueOf)
                    .reduce((x, y) -> x * y).getAsLong();
            System.out.println("Part 2: " + product);
        }
    }

    protected static class Ticket {
        private final List<Integer> numbers;

        public Ticket(final List<Integer> numbers) {
            this.numbers = numbers;
        }

        public boolean isValid(final Collection<? extends Field> fields) {
            return getInvalidNumbers(fields).isEmpty();
        }

        public List<Integer> getInvalidNumbers(final Collection<? extends Field> fields) {
            final List<Integer> list = new ArrayList<>();
            outer:
            for (final var number : numbers) {
                for (final var field : fields) {
                    if (field.contains(number)) {
                        continue outer;
                    }
                }
                list.add(number);
            }
            return Collections.unmodifiableList(list);
        }
    }

    protected static class Field {
        private final String label;
        private final List<Range> ranges;

        public Field(final String label, final List<Range> ranges) {
            this.label = label;
            this.ranges = ranges;
        }

        public boolean contains(final int number) {
            for (final var range : ranges) {
                if (range.contains(number)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return label.hashCode();
        }

        public boolean equals(final Object object) {
            if( this == object ) {
                return true;
            } else if( object == null ) {
                return false;
            }
            try {
                final Field other = ( Field )object;
                return label.equals(other.label);
            } catch( final ClassCastException cce ) {
                return false;
            }
        }
    }

    protected static class Range implements Comparable<Range> {

        private final int minInclusive;
        private final int maxInclusive;

        public Range(final int minInclusive, final int maxInclusive) {
            this.minInclusive = minInclusive;
            this.maxInclusive = maxInclusive;
        }

        public static Range fromString(final String string) {
            final var ends = string.split("-", 2);
            return new Range(Integer.parseInt(ends[0].strip()), Integer.parseInt(ends[1].strip()));
        }

        public int compareTo(final Range other) {
            return Integer.compare(minInclusive, other.minInclusive);
        }

        public boolean contains(final int candidate) {
            return candidate >= minInclusive && candidate <= maxInclusive;
        }

    }
}
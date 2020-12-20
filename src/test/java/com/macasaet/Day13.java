package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day13 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day13.class.getResourceAsStream("/day-13-input.txt"))) {
            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());
            final int earliestDeparture = Integer.parseInt(lines.get(0));
            final var idsString = lines.get(1);
            final var idStrings = idsString.split(",");
            final var candidate = Arrays.stream(idStrings).filter(busId -> !"x".equalsIgnoreCase(busId)).map(Integer::parseInt).map(busId -> {
                if (earliestDeparture % busId == 0) {
                    return new Candidate(busId, 0);
                }
                final int d = earliestDeparture / busId;
                return new Candidate(busId, ((d + 1) * busId) - earliestDeparture);
            }).sorted().findFirst().get();
            final int result = candidate.busId * candidate.timeToWait;
            System.out.println("Part 1: " + result);

            final List<Bus> list = new ArrayList<Bus>(idStrings.length);
            for (int i = 0; i < idStrings.length; i++) {
                final var idString = idStrings[i];
                if ("x".equalsIgnoreCase(idString)) continue;
                final var busId = Long.parseLong(idString);
                list.add(new Bus(busId, i));
            }
            long sum = 0;
            long productOfModSpaces = 1;
            for (int i = list.size(); --i >= 0; ) {
                final var bus = list.get(i);
                productOfModSpaces *= bus.busId;
                long remainder = bus.busId - bus.index;
                remainder = remainder % bus.busId;
                long productOfOtherModSpaces = 1;
                for (int j = list.size(); --j >= 0; ) {
                    if (j == i) continue;
                    final var otherBus = list.get(j);
                    productOfOtherModSpaces *= otherBus.busId;
                }
                final long inverse = findInverse(productOfOtherModSpaces, bus.busId);
                sum += remainder * productOfOtherModSpaces * inverse;
            }
            while (true) {
                final long smallerSolution = sum - productOfModSpaces;
                if (smallerSolution < 0) break;
                sum = smallerSolution;
            }
            System.out.println("Part 2: " + sum);
        }
    }

    protected static long findInverse(final long partialProduct, final long modSpace) {
        for (long multiplier = 1; ; multiplier++) {
            if ((partialProduct * multiplier) % modSpace == 1) {
                return multiplier;
            }
        }
    }

    protected static class Bus {
        final long busId;
        final long index;

        public Bus(final long busId, final long index) {
            this.busId = busId;
            this.index = index;
        }

    }

    protected static class Candidate implements Comparable<Candidate> {
        final int busId;
        final int timeToWait;

        public Candidate(final int busId, final int timeToWait) {
            this.busId = busId;
            this.timeToWait = timeToWait;
        }

        public int compareTo(Candidate other) {
            return Integer.compare(timeToWait, other.timeToWait);
        }
    }

}
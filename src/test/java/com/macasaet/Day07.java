package com.macasaet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 7: The Treachery of Whales ---
 */
public class Day07 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-07.txt"),
                        false);
    }

    /**
     * @return the horizontal position of each crab submarine in the swarm
     */
    protected List<Integer> getCrabPositions() {
        final var list = getInput().collect(Collectors.toList());
        final var line = list.get(0);
        return Arrays.stream(line.split(","))
                .mapToInt(Integer::parseInt)
                .collect(ArrayList::new, List::add, List::addAll);
    }

    /**
     * Assuming a constant fuel consumption rate, calculate the fuel required for the swarm to reach <em>alignmentPoint</em>.
     *
     * @param positions      the starting position of each crab submarine
     * @param alignmentPoint a potential point for the crabs to gather in order to blast a hole in the ocean floor
     * @return the fuel required to reach the alignment point
     */
    protected int calculateConstantFuel(final Iterable<? extends Integer> positions, final int alignmentPoint) {
        int sum = 0;
        for (final var position : positions) {
            sum += Math.abs(alignmentPoint - position);
        }
        return sum;
    }

    /**
     * Calculate the fuel required for the swarm to reach <em>alignmentPoint</em>
     *
     * @param positions      the starting position for each crab submarine
     * @param alignmentPoint a potential point for the crabs to gather in order to blast a hole in the ocean floor
     * @return the fuel required to reach the alignment point
     */
    protected int calculateFuel(final Iterable<? extends Integer> positions, final int alignmentPoint) {
        int sum = 0;
        for (final var position : positions) {
            sum += calculateFuel(position, alignmentPoint);
        }
        return sum;
    }

    /**
     * Calculate the fuel required for a single crab submarine to travel from one horizontal position to the next.
     *
     * @param start the starting position (inclusive)
     * @param end   the ending position (inclusive)
     * @return the amount of fuel consumed in the journey
     */
    protected int calculateFuel(final int start, final int end) {
        final int target = Math.abs(end - start);
        int sum = 0;
        for (int i = target; --i >= 0; ) {
            sum += i + 1;
        }
        return sum;
    }

    @Test
    public final void part1() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        final var positions = getCrabPositions();
        for (final var position : positions) {
            min = Math.min(min, position);
            max = Math.max(max, position);
        }
        final int result = IntStream.range(min, max)
                .map(alignmentPoint -> calculateConstantFuel(positions, alignmentPoint))
                .min()
                .getAsInt();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        final var positions = getCrabPositions();
        for (final var position : positions) {
            min = Math.min(min, position);
            max = Math.max(max, position);
        }
        final int result = IntStream.range(min, max)
                .map(alignmentPoint -> calculateFuel(positions, alignmentPoint))
                .min()
                .getAsInt();
        System.out.println("Part 2: " + result);
    }

}
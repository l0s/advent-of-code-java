package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 4: Camp Cleanup ---
 * <a href="https://adventofcode.com/2022/day/4">https://adventofcode.com/2022/day/4</a>
 */
public class Day04 {

    /**
     * One crew member responsible for cleaning up the camp. They are responsible for a contiguous range of sections.
     *
     * @param sectionMin the lowest section ID for which this Elf is responsible (inclusive)
     * @param sectionMax the highest section ID for which this Elf is responsible (inclusive).
     */
    public record Elf(int sectionMin, int sectionMax) {

        public boolean fullyContains(final Elf other) {
            return sectionMin() <= other.sectionMin() && sectionMax() >= other.sectionMax();
        }

        public static Elf parse(final String string) {
            final var components = string.split("-");
            return new Elf(Integer.parseInt(components[0]), Integer.parseInt(components[1]));
        }
    }

    /**
     * Two elves (of a larger crew) assigned to clean up the camp.
     */
    public record Pair(Elf left, Elf right) {
        public boolean oneFullyContainsTheOther() {
            return left.fullyContains(right) || right.fullyContains(left);
        }
        public boolean hasOverlap() {
            return (left.sectionMin() <= right.sectionMin() && left.sectionMax() >= right.sectionMin())
                    || (right.sectionMin() <= left.sectionMin() && right.sectionMax() >= left.sectionMin());
        }
        public static Pair parse(final String line) {
            final var components = line.split(",");
            return new Pair(Elf.parse(components[0]), Elf.parse(components[1]));
        }
    }
    protected Stream<Pair> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-04.txt"),
                        false)
                .map(Pair::parse);
    }

    @Test
    public final void part1() {
        final var result = getInput().filter(Pair::oneFullyContainsTheOther).count();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var result = getInput().filter(Pair::hasOverlap).count();

        System.out.println("Part 2: " + result);
    }

}
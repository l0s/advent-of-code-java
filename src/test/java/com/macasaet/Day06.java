package com.macasaet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 6: Lanternfish ---
 */
public class Day06 {

    /**
     * A glowing fish that spawns very quickly. Their population grows exponentially.
     */
    public static class Lanternfish {

        private int daysToSpawn;

        /**
         * @param daysToSpawn the number of days until it creates a new {@link Lanternfish}
         */
        public Lanternfish(final int daysToSpawn) {
            setDaysToSpawn(daysToSpawn);
        }

        /**
         * Simulate the passage of one day
         *
         * @return either a new Lanternfish or nothing depending on whether the fish spawned
         */
        public Optional<Lanternfish> tick() {
            final var timer = getDaysToSpawn() - 1;
            if (timer < 0) {
                setDaysToSpawn(6);
                return Optional.of(new Lanternfish(8));
            } else {
                setDaysToSpawn(timer);
                return Optional.empty();
            }
        }

        /**
         * @return the number of days until the fish spawns
         */
        public int getDaysToSpawn() {
            return this.daysToSpawn;
        }

        /**
         * Update this fish's days to spawn
         *
         * @param daysToSpawn the number of days until the fish spawns, must be non-negative
         */
        protected void setDaysToSpawn(final int daysToSpawn) {
            if (daysToSpawn < 0) {
                throw new IllegalArgumentException("\"days to spawn\" must be non-negative");
            }
            this.daysToSpawn = daysToSpawn;
        }
    }

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-06.txt"),
                        false);
    }

    public List<Lanternfish> parseInput() {
        final var list = getInput().toList();
        final var line = list.get(0);
        final var components = line.split(",");
        return Arrays.stream(components)
                .mapToInt(Integer::parseInt)
                .mapToObj(Lanternfish::new)
                .collect(Collectors.toList());
    }

    @Test
    public final void part1() {
        var population = parseInput();
        for (int _i = 80; --_i >= 0; ) {
            final List<Lanternfish> list = new ArrayList<>(population);
            for (final var fish : population) {
                final var result = fish.tick();
                result.ifPresent(list::add);
            }
            population = list;
        }
        System.out.println("Part 1: " + population.size());
    }

    @Test
    public final void part2() {
        final var initial = parseInput();
        var map = new long[9];
        for (final var fish : initial) {
            map[fish.getDaysToSpawn()]++;
        }
        for (int _i = 256; --_i >= 0; ) {
            final var temp = new long[map.length];
            for (int daysToSpawn = map.length; --daysToSpawn >= 0; ) {
                final var count = map[daysToSpawn];
                final var prototype = new Lanternfish(daysToSpawn);
                final var result = prototype.tick();
                temp[prototype.getDaysToSpawn()] += count;
                result.ifPresent(spawn -> temp[spawn.getDaysToSpawn()] = temp[spawn.getDaysToSpawn()] + count);
            }
            map = temp;
        }
        final var result = Arrays.stream(map).reduce(0L, Long::sum);
        System.out.println("Part 2: " + result);
    }

}
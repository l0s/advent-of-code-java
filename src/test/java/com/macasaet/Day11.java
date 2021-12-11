package com.macasaet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 11: Dumbo Octopus ---
 */
public class Day11 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-11.txt"),
                        false);
    }

    /**
     * @return a spatial grid of the cavern indicating the location of the octopuses
     */
    protected Octopus[][] getOctopusGrid() {
        final var list = getInput().map(line -> {
            final var chars = line.toCharArray();
            final byte[] result = new byte[chars.length];
            for (int i = chars.length; --i >= 0; result[i] = (byte) (chars[i] - '0')) ;
            return result;
        }).collect(ArrayList<byte[]>::new, List::add, List::addAll);
        final var result = new Octopus[list.size()][];
        for (int i = list.size(); --i >= 0; ) {
            final var row = list.get(i);
            result[i] = new Octopus[row.length];
            for (int j = row.length; --j >= 0; result[i][j] = new Octopus(i, j, row[j])) ;
        }
        return result;
    }

    /**
     * A rare bioluminescent dumbo octopus
     */
    public static class Octopus {
        private final int x, y;
        private byte energyLevel;

        public Octopus(final int x, final int y, final byte energyLevel) {
            this.x = x;
            this.y = y;
            this.energyLevel = energyLevel;
        }

        /**
         * Increase the octopus' energy level and, if appropriate, propagate side effects to its neighbours.
         *
         * @param population the full population of octopuses
         */
        public void prepareStep(final Population population) {
            if (this.energyLevel > 9) {
                // "An octopus can only flash at most once per step."
                return;
            }
            // "First, the energy level of each octopus increases by 1."
            this.energyLevel++;
            if (this.energyLevel > 9) {
                // "Then, any octopus with an energy level greater than 9 flashes. This increases the energy level of
                // all adjacent octopuses by 1, including octopuses that are diagonally adjacent."
                final var grid = population.grid();
                final var hasRowAbove = x > 0;
                final var hasRowBelow = x < grid.length - 1;
                final var hasColumnToLeft = y > 0;
                final var hasColumnToRight = y < grid[x].length - 1;

                if (hasRowAbove) {
                    grid[x - 1][y].prepareStep(population);
                    if (hasColumnToLeft) {
                        grid[x - 1][y - 1].prepareStep(population);
                    }
                    if (hasColumnToRight) {
                        grid[x - 1][y + 1].prepareStep(population);
                    }
                }
                if (hasColumnToLeft) {
                    grid[x][y - 1].prepareStep(population);
                }
                if (hasColumnToRight) {
                    grid[x][y + 1].prepareStep(population);
                }
                if (hasRowBelow) {
                    grid[x + 1][y].prepareStep(population);
                    if (hasColumnToLeft) {
                        grid[x + 1][y - 1].prepareStep(population);
                    }
                    if (hasColumnToRight) {
                        grid[x + 1][y + 1].prepareStep(population);
                    }
                }
            }
        }

        /**
         * Complete the step and finalise any side effects.
         *
         * @return true if and only if the octopus flashed during this step.
         */
        public boolean finishStep() {
            if (this.energyLevel > 9) {
                // "Finally, any octopus that flashed during this step has its energy level set to 0, as it used all of
                // its energy to flash."
                this.energyLevel = 0;
                return true;
            }
            return false;
        }
    }

    /**
     * The full population of dumbo octopuses. The population members will be modified with each step.
     */
    public record Population(Octopus[][] grid) {
        public int step() {
            for (int i = grid.length; --i >= 0; ) {
                final var row = grid[i];
                for (int j = row.length; --j >= 0; ) {
                    row[j].prepareStep(this);
                }
            }
            int flashes = 0;
            for (int i = grid.length; --i >= 0; ) {
                final var row = grid[i];
                for (int j = row.length; --j >= 0; ) {
                    if (row[j].finishStep()) flashes++;
                }
            }
            return flashes;
        }

    }

    @Test
    public final void part1() {
        final var energyLevels = getOctopusGrid();
        final var population = new Population(energyLevels);

        int flashes = 0;

        for (int step = 0; step < 100; step++) {
            flashes += population.step();
        }

        System.out.println("Part 1: " + flashes);
    }

    @Test
    public final void part2() {
        final var energyLevels = getOctopusGrid();
        final var population = new Population(energyLevels);
        int step = 0;
        while(true) {
            final int flashes = population.step();
            step++;
            if(flashes == 100) {
                System.out.println("Part 2: " + step);
                break;
            }
        }
    }

}
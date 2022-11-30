package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *
 */
public class Day25 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-25.txt"),
                        false);
    }

    public record Herd() {

    }

    public record SeaCucumber() {

    }

    public record OceanFloor(char[][] grid) {
        public static OceanFloor parse(final Stream<String> lines) {
            final var list = lines.toList();
            final var grid = new char[list.size()][];
            for (int i = list.size(); --i >= 0; ) {
                final var line = list.get(i);
                grid[i] = new char[line.length()];
                for (int j = line.length(); --j >= 0; grid[i][j] = line.charAt(j)) ;
            }
            return new OceanFloor(grid);
        }

        public OceanFloor step() {
            return stepEast().stepSouth();
        }

        boolean isOccupied(final int x, final int y) {
            return grid[x][y] != '.';
        }

        char[][] createBlankGrid() {
            final char[][] result = new char[grid().length][];
            for (int i = grid().length; --i >= 0; ) {
                final var originalRow = grid()[i];
                final var newRow = new char[originalRow.length];
                for (int j = originalRow.length; --j >= 0; newRow[j] = '.') ;
                result[i] = newRow;
            }
            return result;
        }

        OceanFloor stepEast() {
            final char[][] copy = createBlankGrid();
            for (int i = grid().length; --i >= 0; ) {
                final var originalRow = grid()[i];
                for (int j = originalRow.length; --j >= 0; ) {
                    final var nextIndex = (j + 1) % originalRow.length;
                    if (originalRow[j] == '>') {
                        if (!isOccupied(i, nextIndex)) {
                            copy[i][nextIndex] = '>';
                        } else {
                            copy[i][j] = '>';
                        }
                    } else if (originalRow[j] != '.') {
                        copy[i][j] = originalRow[j];
                    }
                }
            }
            return new OceanFloor(copy);
        }

        OceanFloor stepSouth() {
            final char[][] copy = createBlankGrid();
            for (int i = grid().length; --i >= 0; ) {
                final var originalRow = grid()[i];
                final var nextIndex = (i + 1) % grid().length;
                for (int j = originalRow.length; --j >= 0; ) {
                    if (originalRow[j] == 'v') {
                        if (!isOccupied(nextIndex, j)) {
                            copy[nextIndex][j] = 'v';
                        } else {
                            copy[i][j] = 'v';
                        }
                    } else if (originalRow[j] != '.') {
                        copy[i][j] = originalRow[j];
                    }
                }
            }
            return new OceanFloor(copy);
        }

        public String toString() {
            final var builder = new StringBuilder();
            for (final var row : grid()) {
                builder.append(row).append('\n');
            }
            return builder.toString();
        }

        public int hashCode() {
            int result = 1;
            for (final var row : grid()) {
                result += 31 * result + Arrays.hashCode(row);
            }
            return result;
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            } else if (this == o) {
                return true;
            }
            try {
                final OceanFloor other = (OceanFloor) o;
                if (grid().length != other.grid().length) {
                    return false;
                }
                for (int i = grid.length; --i >= 0; ) {
                    final var mine = grid()[i];
                    final var theirs = other.grid()[i];
                    if (!Arrays.equals(mine, theirs)) {
                        return false;
                    }
                }
                return true;
            } catch (final ClassCastException _cce) {
                return false;
            }
        }
    }

    @Test
    public final void part1() {
        var oceanFloor = OceanFloor.parse(getInput());
        for (int i = 1; ; i++) {
            final var next = oceanFloor.step();
            if (next.equals(oceanFloor)) {
                System.out.println("Part 1: " + i);
                break;
            }
            oceanFloor = next;
        }
    }

    @Test
    public final void part2() {

        System.out.println("Part 2: " + null);
    }

}
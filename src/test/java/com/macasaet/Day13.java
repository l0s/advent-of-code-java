package com.macasaet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 13: Transparent Origami ---
 */
public class Day13 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-13.txt"),
                        false);
    }

    /**
     * A point on the translucent sheet of paper. Note that <em>x</em> and <em>y</em> correspond to a particular
     * {@link Axis}.
     */
    public record Point(int x, int y) {
    }

    /**
     * An axis of the translucent sheet of paper
     */
    public enum Axis {
        /**
         * The axis that increases to the right
         */
        X,

        /**
         * The axis that increases downward
         */
        Y,
    }

    /**
     * An equation for a line
     */
    public record Line(Axis axis, int value) {
        public String toString() {
            return switch (axis()) {
                case X -> "x=" + value;
                case Y -> "y=" + value;
            };
        }
    }

    public record Input(Collection<Point> points, List<Line> folds, int maxX, int maxY) {
        public Sheet getSheet() {
            final boolean[][] grid = new boolean[maxY + 1][];
            for (int i = grid.length; --i >= 0; ) {
                grid[i] = new boolean[maxX + 1];
            }
            for (final var point : points) {
                /*  The first value, x, increases to the right. The second value, y, increases downward. */
                grid[point.y()][point.x()] = true;
            }
            return new Sheet(grid);
        }
    }

    /**
     * A sheet of translucent paper
     */
    public record Sheet(boolean[][] grid) {

        public int countDots() {
            int result = 0;
            final var grid = grid();
            for (int i = grid.length; --i >= 0; ) {
                for (int j = grid[i].length; --j >= 0; ) {
                    if (grid[i][j]) {
                        result++;
                    }
                }
            }
            return result;
        }

        public String toString() {
            final var builder = new StringBuilder();
            for (final var row : grid) {
                for (final var cell : row) {
                    builder.append(cell ? '#' : '.');
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        public Sheet fold(final Line line) {
            // note, value is always positive
            return switch (line.axis()) {
                case X -> {
                    // fold along the x-axis (vertical line)
                    final var newGrid = new boolean[grid.length][];
                    for (int i = newGrid.length; --i >= 0; ) {
                        final var newRow = new boolean[line.value() + 1];
                        for (int j = newRow.length; --j >= 0; newRow[j] = grid[i][j]) ;
                        for(int j = grid[i].length - line.value(); --j > 0;  ) {
                            if(grid[i][line.value() + j]) {
                                newRow[line.value() - j] = true;
                            }
                        }
                        newGrid[i] = newRow;
                    }
                    yield new Sheet(newGrid);
                }
                case Y -> {
                    // fold along the y-axis (horizontal line)
                    final var newGrid = new boolean[line.value()][];
                    for (int i = newGrid.length; --i >= 0; ) {
                        final var newRow = new boolean[grid[i].length];
                        for (int j = grid[i].length; --j >= 0; newRow[j] = grid[i][j]) ;
                        newGrid[i] = newRow;
                    }
                    for (int i = grid.length - line.value(); --i > 0; ) {
                        final var oldRow = grid[line.value() + i];
                        for (int j = oldRow.length;
                             --j >= 0;
                             newGrid[line.value() - i][j] |= oldRow[j])
                            ;
                    }
                    yield new Sheet(newGrid);
                }
            };
        }
    }

    public Input parseInput() {
        int section = 0;
        final var points = new HashSet<Point>();
        final var folds = new ArrayList<Line>();
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (final var line : getInput().collect(Collectors.toList())) {
            if (line.isBlank()) {
                section++;
                continue;
            }
            if (section == 0) { // points
                final var components = line.split(",");
                final var x = Integer.parseInt(components[0]);
                maxX = Math.max(x, maxX);
                final var y = Integer.parseInt(components[1]);
                maxY = Math.max(y, maxY);
                final var point = new Point(x, y);
                points.add(point);
            } else { // commands
                final var equation = line.replaceFirst("fold along ", "");
                final var components = equation.split("=");
                final var axis = Axis.valueOf(components[0].toUpperCase(Locale.ROOT));
                final var value = Integer.parseInt(components[1]);
                final var fold = new Line(axis, value);
                folds.add(fold);
            }
        }
        return new Input(points, folds, maxX, maxY);
    }

    @Test
    public final void part1() {
        final var input = parseInput();
        final var sheet = input.getSheet();
        final var firstFold = input.folds().get(0);
        final var result = sheet.fold(firstFold);
        System.out.println("Part 1: " + result.countDots());
    }

    @Test
    public final void part2() {
        final var input = parseInput();
        var sheet = input.getSheet();
        for (final var fold : input.folds()) {
            sheet = sheet.fold(fold);
        }
        System.out.println("Part 2:\n" + sheet);
    }

}

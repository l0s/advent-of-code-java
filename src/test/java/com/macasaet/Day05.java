package com.macasaet;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 5: Hydrothermal Venture ---
 */
public class Day05 {

    /**
     * A point on the ocean floor
     */
    public static record Point(int x, int y) {
        public static Point parse(final String string) {
            final var components = string.split(",");
            return new Point(Integer.parseInt(components[0]), Integer.parseInt(components[1]));
        }

    }

    /**
     * A portion of the ocean floor on which there are hydrothermal vents, which produce large, opaque clouds
     */
    public static record LineSegment(Point start, Point end) {
        public static LineSegment parse(final String string) {
            final var components = string.split(" -> ");
            return new LineSegment(Point.parse(components[0]), Point.parse(components[1]));
        }

        /**
         * Highlight the location of this line segment on the diagram. Each cell that this segment covers will have its
         * value incremented. The higher the number, the more vents cover the cell.
         *
         * @param diagram A map of the ocean floor which will be updated by this call.
         */
        public void update(final int[][] diagram) {
            /*
            "Because of the limits of the hydrothermal vent mapping system, the lines in your list will only ever be
            horizontal, vertical, or a diagonal line at exactly 45 degrees."
             */
            final int horizontalStep = start().x() == end().x()
                    ? 0
                    : start().x() < end().x()
                    ? 1
                    : -1;
            final int verticalStep = start().y() == end().y()
                    ? 0
                    : start().y() < end().y()
                    ? 1
                    : -1;
            final Predicate<Integer> xTester = start().x() == end().x()
                    ? x -> true
                    : start().x() < end().x()
                    ? x -> x <= end().x()
                    : x -> x >= end().x();
            final Predicate<Integer> yTester = start().y() == end().y()
                    ? y -> true
                    : start().y() < end().y()
                    ? y -> y <= end().y()
                    : y -> y >= end().y();

            for (int i = start().x(), j = start().y();
                 xTester.test(i) && yTester.test(j);
                 i += horizontalStep, j += verticalStep) {
                diagram[i][j]++;
            }
        }

        public int lowestX() {
            return Math.min(start().x(), end().x());
        }

        public int highestX() {
            return Math.max(start().x(), end().x());
        }

        public int lowestY() {
            return Math.min(start().y(), end().y());
        }

        public int highestY() {
            return Math.max(start().y(), end().y());
        }

        public String toString() {
            return start() + " -> " + end();
        }
    }

    protected Stream<LineSegment> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-05.txt"),
                        false)
                .map(LineSegment::parse);
    }

    protected record Extremes(int lowestX, int lowestY, int highestX, int highestY) {
        public Extremes combine(final LineSegment segment) {
            return new Extremes(Math.min(lowestX(), segment.lowestX()),
                    Math.min(lowestY(), segment.lowestY()),
                    Math.max(highestX(), segment.highestX()),
                    Math.max(highestY(), segment.highestY()));
        }

        public Extremes combine(final Extremes other) {
            return new Extremes(Math.min(lowestX(), other.lowestX()),
                    Math.min(lowestY(), other.lowestY()),
                    Math.max(highestX(), other.highestX()),
                    Math.max(highestY(), other.highestY()));
        }

        public int[][] createBlankDiagram() {
            final int[][] result = new int[highestX() + 1][];
            for (int i = result.length; --i >= 0; result[i] = new int[highestY() + 1]) ;
            return result;
        }
    }

    @Test
    public final void part1() {
        final var segments = getInput()
                // "For now, only consider horizontal and vertical lines: lines where either x1 = x2 or y1 = y2."
                .filter(segment -> segment.start().x() == segment.end().x() || segment.start().y() == segment.end().y())
                .collect(Collectors.toList());

        final var extremes = segments
                .stream()
                .reduce(new Extremes(0, 0, 0, 0),
                        Extremes::combine,
                        Extremes::combine);
        // there are no negative values
        // Note, we could save a little bit of space and time by using a smaller map since none of the line segments
        // need point 0,0. However, the savings are likely negligible.
        final int[][] diagram = extremes.createBlankDiagram();

        for (final var segment : segments) {
            segment.update(diagram);
        }
        int sum = 0;
        for (int i = diagram.length; --i >= 0; ) {
            final var row = diagram[i];
            for (int j = row.length; --j >= 0; ) {
                if (row[j] >= 2) {
                    sum++;
                }
            }
        }
        System.out.println("Part 1: " + sum);
    }

    @Test
    public final void part2() {
        /*
        "Unfortunately, considering only horizontal and vertical lines doesn't give you the full picture; you need to
        also consider diagonal lines."
         */
        final var segments = getInput()
                .collect(Collectors.toList());
        final var extremes = segments
                .stream()
                .reduce(new Extremes(0, 0, 0, 0),
                        Extremes::combine,
                        Extremes::combine);
        // there are no negative values
        // Note, we could save a little bit of space and time by using a smaller map since none of the line segments
        // need point 0,0. However, the savings are likely negligible.
        final int[][] diagram = extremes.createBlankDiagram();
        for (final var segment : segments) {
            segment.update(diagram);
        }
        int sum = 0;
        for (int i = diagram.length; --i >= 0; ) {
            final var row = diagram[i];
            for (int j = row.length; --j >= 0; ) {
                if (row[j] >= 2) {
                    sum++;
                }
            }
        }
        System.out.println("Part 2: " + sum);
    }

}
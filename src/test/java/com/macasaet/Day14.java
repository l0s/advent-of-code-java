package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * --- Day 14: Regolith Reservoir ---
 * <a href="https://adventofcode.com/2022/day/14">https://adventofcode.com/2022/day/14</a>
 */
public class Day14 {

    public enum Cell {
        ROCK,
        SAND
    }

    record Coordinate(int verticalDepth, int horizontalOffset) {

        public static Coordinate parse(final String string) {
            final var components = string.split(",");
            final var verticalDepth = Integer.parseInt(components[1]);
            final var horizontalOffset = Integer.parseInt(components[0]);
            return new Coordinate(verticalDepth, horizontalOffset);
        }
    }

    public record Cave(Map<Integer, Map<Integer, Cell>> grid, int maxDepth, int minHorizontalOffset, int maxHorizontalOffset) {

        public int pourSandIntoAbyss() {
            int settledSand = 0;
            while(true) {
                var fallingSandCoordinate = new Coordinate(0, 500);
                while(true) {
                    final var next = getNextCoordinate(fallingSandCoordinate, null);
                    if(next != null) {
                        fallingSandCoordinate = next;
                        if(fallingSandCoordinate.verticalDepth() >= maxDepth()) {
                            return settledSand;
                        }
                    } else {
                        final var row = grid.computeIfAbsent(fallingSandCoordinate.verticalDepth(), key -> new HashMap<>());
                        row.put(fallingSandCoordinate.horizontalOffset(), Cell.SAND);
                        settledSand++;
                        break;
                    }
                }
            }
        }

        public int fillAperture() {
            int settledSand = 0;
            while(true) {
                var fallingSandCoordinate = new Coordinate(0, 500);
                while(true) {
                    final var next = getNextCoordinate(fallingSandCoordinate, floorDepth());
                    if(next != null) {
                        fallingSandCoordinate = next;
                    } else {
                        final var secondRow = grid().computeIfAbsent(1, key -> new HashMap<>());
                        if(secondRow.containsKey(499) && secondRow.containsKey(500) && secondRow.containsKey(501)) {
                            return settledSand + 1;
                        }
                        final var row = grid.computeIfAbsent(fallingSandCoordinate.verticalDepth(), key -> new HashMap<>());
                        row.put(fallingSandCoordinate.horizontalOffset(), Cell.SAND);
                        settledSand++;
                        break;
                    }
                }
            }
        }

        int floorDepth() {
            return maxDepth() + 2;
        }

        Coordinate getNextCoordinate(final Coordinate start, Integer floorDepth) {
            final var x = start.verticalDepth();
            final var y = start.horizontalOffset();
            if(floorDepth != null && x + 1 >= floorDepth) {
                return null;
            }
            final var nextRow = grid().computeIfAbsent(x + 1, key -> new HashMap<>());
            if(!nextRow.containsKey(y)) {
                return new Coordinate(x + 1, y);
            } else if(!nextRow.containsKey(y - 1)) {
                return new Coordinate(x + 1, y - 1);
            } else if(!nextRow.containsKey(y + 1)) {
                return new Coordinate(x + 1, y + 1);
            }
            return null;
        }

        public static Cave parse(final Collection<? extends String> lines) {
            int maxDepth = 0;
            int maxHorizontalOffset = Integer.MIN_VALUE;
            int minHorizontalOffset = Integer.MAX_VALUE;

            final var grid = new HashMap<Integer, Map<Integer, Cell>>();
            for(final var line : lines) {
                final var rockPath = parseRockPaths(line);
                var last = rockPath.get(0);
                if(last.verticalDepth() > maxDepth) {
                    maxDepth = last.verticalDepth();
                }
                if(last.horizontalOffset() < minHorizontalOffset) {
                    minHorizontalOffset = last.horizontalOffset();
                }
                if(last.horizontalOffset() > maxHorizontalOffset) {
                    maxHorizontalOffset = last.horizontalOffset();
                }
                for(int i = 1; i < rockPath.size(); i++) {
                    final var current = rockPath.get(i);
                    if(last.verticalDepth() == current.verticalDepth()) {
                        // horizontal line
                        int start;
                        int end;
                        if(last.horizontalOffset() < current.horizontalOffset()) {
                            start = last.horizontalOffset();
                            end = current.horizontalOffset();
                        } else {
                            start = current.horizontalOffset();
                            end = last.horizontalOffset();
                        }
                        final var row = grid.computeIfAbsent(last.verticalDepth(), key -> new HashMap<>());
                        for(int y = start; y <= end; y++) {
                            row.put(y, Cell.ROCK);
                        }
                    } else {
                        if(last.horizontalOffset() != current.horizontalOffset()) {
                            throw new IllegalStateException("Line segments are not on the same vertical axis");
                        }
                        // vertical line
                        int start;
                        int end;
                        if(last.verticalDepth() < current.verticalDepth()) {
                            start = last.verticalDepth();
                            end = current.verticalDepth();
                        } else {
                            start = current.verticalDepth();
                            end = last.verticalDepth();
                        }
                        for(int x = start; x <= end; x++) {
                            final var row = grid.computeIfAbsent(x, key -> new HashMap<>());
                            row.put(last.horizontalOffset(), Cell.ROCK);
                        }
                    }
                    if(current.verticalDepth() > maxDepth) {
                        maxDepth = current.verticalDepth();
                    }
                    if(current.horizontalOffset() < minHorizontalOffset) {
                        minHorizontalOffset = current.horizontalOffset();
                    }
                    if(current.horizontalOffset() > maxHorizontalOffset) {
                        maxHorizontalOffset = current.horizontalOffset();
                    }
                    last = current;
                }
            }
            return new Cave(grid, maxDepth, minHorizontalOffset, maxHorizontalOffset);
        }

        static List<Coordinate> parseRockPaths(final String line) {
            return Arrays.stream(line.split(" -> ")).map(Coordinate::parse).toList();
        }

        @Override
        public String toString() {
            final var buffer = new StringBuilder();
            for(int i = 0; i <= floorDepth(); i++) {
                buffer.append(i).append(' ');
                final var row = grid.getOrDefault(i, Collections.emptyMap());
                for(int j = minHorizontalOffset(); j <= maxHorizontalOffset(); j++) {
                    final var cell = row.get(j);
                    final char marker = cell == null ? ' ' : Cell.ROCK.equals(cell) ? '#' : 'o';
                    buffer.append(marker);
                }
                buffer.append('\n');
            }
            return buffer.toString();
        }
    }

    protected Cave getInput() {
        final var lines = StreamSupport.stream(new LineSpliterator("day-14.txt"), false)
                .toList();
        return Cave.parse(lines);
    }

    @Test
    public final void part1() {
        final var cave = getInput();
        final var result = cave.pourSandIntoAbyss();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var cave = getInput();
        final var result = cave.fillAperture();

        System.out.println("Part 2: " + result);
    }

}
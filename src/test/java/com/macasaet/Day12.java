package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.stream.StreamSupport;

/**
 * --- Day 12: Hill Climbing Algorithm ---
 * <a href="https://adventofcode.com/2022/day/12">https://adventofcode.com/2022/day/12</a>
 */
public class Day12 {

    record Coordinate(int x, int y) {
    }

    public record HeightMap(int[][] grid, Coordinate start, Coordinate end) {

        public int lengthOfShortestPath() {
            return this.lengthOfShortestPath(this.start);
        }

        public int lengthOfShortestPath(final Coordinate startingPoint) {
                final var cheapestCostToNode = new HashMap<Coordinate, Integer>();
                cheapestCostToNode.put(startingPoint, 0);
                final var estimatedCostToFinish = new HashMap<Coordinate, Integer>();
                estimatedCostToFinish.put(startingPoint, estimateDistance(startingPoint, this.end));
                final var openSet = new PriorityQueue<Coordinate>((x, y) -> {
                    final var xScore = estimatedCostToFinish.getOrDefault(x, Integer.MAX_VALUE);
                    final var yScore = estimatedCostToFinish.getOrDefault(y, Integer.MAX_VALUE);
                    return xScore.compareTo(yScore);
                });
                openSet.add(startingPoint);
                while (!openSet.isEmpty()) {
                    final var current = openSet.remove();
                    if (current.equals(this.end)) {
                        return cheapestCostToNode.get(current);
                    }
                    for (final var neighbour : neighbours(current)) {
                        final var tentativeGScore = cheapestCostToNode.get(current) + 1;
                        if (tentativeGScore < cheapestCostToNode.getOrDefault(neighbour, Integer.MAX_VALUE)) {
                            cheapestCostToNode.put(neighbour, tentativeGScore);
                            estimatedCostToFinish.put(neighbour, tentativeGScore + estimateDistance(neighbour, this.end));
                            if (!openSet.contains(neighbour)) {
                                openSet.add(neighbour);
                            }
                        }
                    }
                }
                return Integer.MAX_VALUE;
            }

            public List<Coordinate> getPotentialTrailHeads() {
                final var list = new ArrayList<Coordinate>();
                for(int i = this.grid().length; --i >= 0; ) {
                    final var row = this.grid()[i];
                    for(int j = row.length; --j >= 0; ) {
                        if(row[j] == 0) {
                            list.add(new Coordinate(i, j));
                        }
                    }
                }
                return Collections.unmodifiableList(list);
            }

            int height(final Coordinate coordinate) {
                return this.grid[coordinate.x()][coordinate.y()];
            }

            List<Coordinate> neighbours(final Coordinate coordinate) {
                final var list = new ArrayList<Coordinate>(4);
                if (coordinate.x() > 0) {
                    final var up = new Coordinate(coordinate.x() - 1, coordinate.y());
                    if (height(coordinate) + 1 >= height(up)) {
                        list.add(up);
                    }
                }
                if (coordinate.x() < this.grid.length - 1) {
                    final var down = new Coordinate(coordinate.x() + 1, coordinate.y());
                    if (height(coordinate) + 1 >= height(down)) {
                        list.add(down);
                    }
                }
                if (coordinate.y() > 0) {
                    final var left = new Coordinate(coordinate.x(), coordinate.y() - 1);
                    if (height(coordinate) + 1 >= height(left)) {
                        list.add(left);
                    }
                }
                final var row = this.grid[coordinate.x()];
                if (coordinate.y() < row.length - 1) {
                    final var right = new Coordinate(coordinate.x(), coordinate.y() + 1);
                    if (height(coordinate) + 1 >= height(right)) {
                        list.add(right);
                    }
                }
                return Collections.unmodifiableList(list);
            }

            int estimateDistance(final Coordinate from, final Coordinate to) {
                return (int) Math.sqrt(Math.pow(from.x() - to.x(), 2.0) + Math.pow(from.y() - to.y(), 2.0));
            }

        }

    protected HeightMap getInput() {
        final var charGrid = StreamSupport.stream(new LineSpliterator("day-12.txt"), false).map(line -> {
            final var list = new ArrayList<Character>(line.length());
            for (final var c : line.toCharArray()) {
                list.add(c);
            }
            return list;
        }).toList();
        Coordinate origin = null;
        Coordinate destination = null;
        int[][] grid = new int[charGrid.size()][];
        for(int i = charGrid.size(); --i >= 0; ) {
            final var row = charGrid.get(i);
            grid[i] = new int[row.size()];
            for(int j = row.size(); --j >= 0; ) {
                final char c = row.get(j);
                if(c == 'S') {
                    origin = new Coordinate(i, j);
                    grid[i][j] = 0;
                } else if(c == 'E') {
                    destination = new Coordinate(i, j);
                    grid[i][j] = 'z' - 'a';
                } else {
                    grid[i][j] = c - 'a';
                }
            }
        }
        Objects.requireNonNull(origin);
        Objects.requireNonNull(destination);
        return new HeightMap(grid, origin, destination);
    }

    @Test
    public final void part1() {
        final var map = getInput();
        final var result = map.lengthOfShortestPath();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var map = getInput();
        var result = Integer.MAX_VALUE;
        for(final var candidate : map.getPotentialTrailHeads()) {
            final var length = map.lengthOfShortestPath(candidate);
            if(length < result) {
                result = length;
            }
        }

        System.out.println("Part 2: " + result);
    }

}
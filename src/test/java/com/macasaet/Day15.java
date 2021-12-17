package com.macasaet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 15: Chiton ---
 */
public class Day15 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-15.txt"),
                        false);
    }

    protected int[][] getGrid() {
        final var list = getInput().collect(Collectors.toList());
        final int[][] grid = new int[list.size()][];
        for (int i = 0; i < grid.length; i++) {
            final var chars = list.get(i).toCharArray();
            final var row = new int[chars.length];
            for (int j = chars.length; --j >= 0; row[j] = chars[j] - '0') ;
            grid[i] = row;
        }
        return grid;
    }

    public record Point(int x, int y) {

        public int risk(int[][] risks) {
            return risks[x][y];
        }

    }

    public record Cavern(int[][] grid) {

        public Set<Point> predecessors(final Point source) {
            final var result = new HashSet<Point>();
            if (source.x() > 0) {
                result.add(new Point(source.x() - 1, source.y()));
            }
            if (source.y() > 0) {
                result.add(new Point(source.x(), source.y() - 1));
            }
            return Collections.unmodifiableSet(result);
        }

        public Set<Point> successors(final Point source) {
            final var result = new HashSet<Point>();
            if (source.x() < grid().length - 1) {
                result.add(new Point(source.x() + 1, source.y()));
            }
            if (source.y() < grid()[source.x()].length - 1) {
                result.add(new Point(source.x(), source.y() + 1));
            }
            return Collections.unmodifiableSet(result);
        }

        public Cavern explode() {
            final int[][] largeGrid = new int[grid.length * 5][];
            for (int i = largeGrid.length; --i >= 0; ) {
                largeGrid[i] = new int[grid.length * 5];
            }
            for (int i = grid.length; --i >= 0; ) {
                for (int j = grid.length; --j >= 0; ) {
                    largeGrid[i][j] = grid[i][j];
                }
            }
            for (int tileRow = 0; tileRow < 5; tileRow++) {
                for (int tileColumn = 0; tileColumn < 5; tileColumn++) {
                    if (tileRow > 0) {
                        for (int i = grid.length; --i >= 0; ) {
                            for (int j = grid.length; --j >= 0; ) {
                                // copy from row above
                                int value = largeGrid[(tileRow - 1) * grid.length + i][tileColumn * grid.length + j] + 1;
                                if (value == 10) {
                                    value = 1;
                                }
                                largeGrid[tileRow * grid.length + i][tileColumn * grid.length + j] = value;
                            }
                        }
                    } else if (tileColumn > 0) {
                        for (int i = grid.length; --i >= 0; ) {
                            for (int j = grid.length; --j >= 0; ) {
                                // copy from column to the left
                                int value = largeGrid[tileRow * grid.length + i][(tileColumn - 1) * grid.length + j] + 1;
                                if (value == 10) {
                                    value = 1;
                                }
                                largeGrid[tileRow * grid.length + i][tileColumn * grid.length + j] = value;
                            }
                        }
                    }
                }
            }
            return new Cavern(largeGrid);
        }

        public long[][] calculateCumulativeRisk() {
            final var cumulative = new long[grid().length][];
            for (int i = cumulative.length; --i >= 0; cumulative[i] = new long[grid()[i].length]) ;
            final var visited = new HashSet<Point>();
            final var queue = new LinkedList<Point>();
            final var destination = new Point(grid().length - 1, grid()[grid().length - 1].length - 1);
            queue.add(destination);
            visited.add(destination);

            while (!queue.isEmpty()) {
                final var node = queue.remove();
                final var successors = successors(node);
                if (successors.isEmpty()) {
                    // destination
                    cumulative[node.x][node.y] = node.risk(grid());
                } else {
                    var minSuccessorRisk = Long.MAX_VALUE;
                    for (final var successor : successors) {
                        if (!visited.contains(successor)) {
                            throw new IllegalStateException("Successor has not been visited");
                        }
                        minSuccessorRisk = Math.min(minSuccessorRisk, cumulative[successor.x][successor.y]);
                    }
                    cumulative[node.x][node.y] = node.risk(grid()) + minSuccessorRisk;
                }

                for (final var predecessor : predecessors(node)) {
                    if (!visited.contains(predecessor)) {
                        queue.add(predecessor);
                        visited.add(predecessor);
                    }
                }
            }
            return cumulative;
        }

        /**
         * @return the risk level associated with the path through the cavern that avoids the most chitons
         */
        public int lowestRiskThroughTheCavern() {
            // the lowest known risk from origin to a given node
            final var lowestRiskToNode = new HashMap<Point, Integer>();
            // the estimated risk from origin to destination if it goes through a given node
            final var estimatedRiskThroughNode = new HashMap<Point, Integer>();
            final var openSet = new PriorityQueue<Point>(Comparator.comparing(estimatedRiskThroughNode::get));

            for (int i = grid().length; --i >= 0; ) {
                final var row = grid()[i];
                for (int j = row.length; --j >= 0; ) {
                    final var point = new Point(i, j);
                    if (i == 0 && j == 0) {
                        lowestRiskToNode.put(point, 0);
                        estimatedRiskThroughNode.put(point, manhattanDistance(point));
                        openSet.add(point);
                    } else {
                        lowestRiskToNode.put(point, Integer.MAX_VALUE);
                        estimatedRiskThroughNode.put(point, Integer.MAX_VALUE);
                    }
                }
            }

            while(!openSet.isEmpty()) {
                final var current = openSet.poll();
                if(current.x() == grid().length - 1 && current.y() == grid()[grid().length - 1].length - 1) {
                    return lowestRiskToNode.get(current);
                }
                final var lowestRiskToCurrent = lowestRiskToNode.get(current);
                for(final var neighbour : neighbours(current)) {
                    final var tentativeRisk = lowestRiskToCurrent + neighbour.risk(grid());
                    if(tentativeRisk < lowestRiskToNode.get(neighbour)) {
                        lowestRiskToNode.put(neighbour, tentativeRisk);
                        estimatedRiskThroughNode.put(neighbour, tentativeRisk + manhattanDistance(neighbour));
                        if(!openSet.contains(neighbour)) {
                            openSet.add(neighbour);
                        }
                    }
                }
            }
            throw new IllegalStateException("No path out of the cavern!");
        }

        /**
         * @param point
         * @return
         */
        protected int manhattanDistance(Point point) {
            return Math.abs(point.x() - (grid().length - 1))
                    + Math.abs(point.y() - (grid()[grid().length - 1].length - 1));
        }

        public Set<Point> neighbours(final Point point) {
            final var result = new HashSet<Point>();
            if (point.x() > 0) {
                result.add(new Point(point.x() - 1, point.y()));
            }
            if (point.x() < grid().length - 1) {
                result.add(new Point(point.x() + 1, point.y()));
            }
            if (point.y() > 0) {
                result.add(new Point(point.x(), point.y() - 1));
            }
            if (point.y() < grid()[point.x()].length - 1) {
                result.add(new Point(point.x(), point.y() + 1));
            }
            return Collections.unmodifiableSet(result);
        }

    }

    @Test
    public final void part1() {
        final var grid = getGrid();
        final var cavern = new Cavern(grid);
        System.out.println("Part 1: " + cavern.lowestRiskThroughTheCavern());
    }

    @Test
    public final void part2() {
        final var grid = getGrid();
        final var cavern = new Cavern(grid).explode();
        System.out.println("Part 2: " + cavern.lowestRiskThroughTheCavern());
    }

}
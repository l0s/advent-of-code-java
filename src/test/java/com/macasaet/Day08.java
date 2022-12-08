package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * --- Day 8: Treetop Tree House ---
 * <a href="https://adventofcode.com/2022/day/8">https://adventofcode.com/2022/day/8</a>
 */
public class Day08 {

    record Forest(int[][] grid) {
        public int countVisible() {
            int result = 0;
            for(int i = grid().length; --i >= 0; ) {
                final var row = grid()[i];
                for(int j = row.length; --j >= 0; ) {
                    if(isVisible(i, j)) {
                        result++;
                    }
                }
            }
            return result;
        }

        public int scenicScore(final int x, final int y) {
            final int treeHeight = grid()[x][y];
            int northScore = 0;
            for(int i = x; --i >= 0; ) {
                final var height = grid()[i][y];
                northScore += 1;
                if(height >= treeHeight) {
                    break;
                }
            }
            int southScore = 0;
            for(int i = x + 1; i < grid().length; i++) {
                final var height = grid()[i][y];
                southScore += 1;
                if(height >= treeHeight) {
                    break;
                }
            }
            int westScore = 0;
            for(int j = y; --j >= 0; ) {
                final var height = grid()[x][j];
                westScore += 1;
                if(height >= treeHeight) {
                    break;
                }
            }
            int eastScore = 0;
            for(int j = y + 1; j < grid()[x].length; j++) {
                final var height = grid()[x][j];
                eastScore += 1;
                if(height >= treeHeight) {
                    break;
                }
            }
            return northScore * eastScore * southScore * westScore;
        }

        boolean isVisible(final int x, final int y) {
            if(x == 0 || x == grid().length || y == 0 || y == grid()[x].length) {
                // trees on the edge
                return true;
            }
            final int treeHeight = grid()[x][y];
            if (!isObstructedFromTheNorth(x, y, treeHeight)) {
                return true;
            }
            if (!isObstructedFromTheSouth(x, y, treeHeight)) {
                return true;
            }
            if (!isObstructedFromTheWest(x, y, treeHeight)) {
                return true;
            }
            if (!isObstructedFromTheEast(x, y, treeHeight)) {
                return true;
            }
            return false;
        }

        private boolean isObstructedFromTheEast(int x, int y, int treeHeight) {
            for(int j = grid()[x].length; --j > y; ) {
                if(grid()[x][j] >= treeHeight) {
                    return true;
                }
            }
            return false;
        }

        private boolean isObstructedFromTheWest(int x, int y, int treeHeight) {
            for(int j = y; --j >= 0; ) {
                if(grid()[x][j] >= treeHeight) {
                    return true;
                }
            }
            return false;
        }

        private boolean isObstructedFromTheSouth(int x, int y, int treeHeight) {
            for(int i = grid().length; --i > x; ) {
                if(grid()[i][y] >= treeHeight) {
                    return true;
                }
            }
            return false;
        }

        private boolean isObstructedFromTheNorth(int x, int y, int treeHeight) {
            for(int i = x; --i >= 0; ) {
                if(grid()[i][y] >= treeHeight) {
                    return true;
                }
            }
            return false;
        }
    }

    protected Forest getInput() {
        final var list = StreamSupport
                .stream(new LineSpliterator("day-08.txt"),
                        false)
                .map(line -> {
                    final var chars = line.toCharArray();
                    final var row = new int[chars.length];
                    for(int i = chars.length; --i >= 0; row[i] = chars[i] - '0');
                    return row;
                })
                .collect(Collectors.toList());
        final var grid = new int[list.size()][];
        for(int i = list.size(); --i >= 0; grid[i] = list.get(i));
        return new Forest(grid);
    }

    @Test
    public final void part1() {
        final var forest = getInput();
        final var result = forest.countVisible();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var forest = getInput();
        int result = Integer.MIN_VALUE;
        for(int i = forest.grid().length; --i >= 0; ) {
            for( int j = forest.grid.length; --j >= 0; ) {
                final var score = forest.scenicScore(i, j);
                if(score > result) {
                    result = score;
                }
            }
        }
        System.out.println("Part 2: " + result);
    }

}
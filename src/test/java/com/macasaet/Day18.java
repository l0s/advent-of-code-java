package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.StreamSupport;

/**
 * --- Day 18: Boiling Boulders ---
 * <a href="https://adventofcode.com/2022/day/18">https://adventofcode.com/2022/day/18</a>
 */
public class Day18 {

    public static final int SCAN_DIMENSION = 32;

    protected static Droplet getInput() {
        final var cubeCoordinates = StreamSupport.stream(new LineSpliterator("day-18.txt"), false)
                .map(Cube::parse)
                .toList();
        return new Droplet(cubeCoordinates);
    }

    @Test
    public final void part1() {
        final var droplet = getInput();
        final var result = droplet.surfaceArea(CubeType.Air);

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var droplet = getInput();
        droplet.immerse();
        final var result = droplet.surfaceArea(CubeType.Water);

        System.out.println("Part 2: " + result);
    }

    public enum CubeType {
        Air,
        Lava,
        Water,
    }

    record Cube(int x, int y, int z) {
        public static Cube parse(final String line) {
            final var components = line.split(",");
            return new Cube(Integer.parseInt(components[0]), Integer.parseInt(components[1]), Integer.parseInt(components[2]));
        }

        public CubeType getType(final CubeType[][][] grid) {
            return grid[x()][y()][z()];
        }

        public void setType(final CubeType[][][] grid, final CubeType type) {
            grid[x()][y()][z()] = type;
        }
    }

    public static class Droplet {

        private final Collection<? extends Cube> cubes;
        private final CubeType[][][] grid;

        public Droplet(final Collection<? extends Cube> cubes) {
            final CubeType[][][] grid = new CubeType[SCAN_DIMENSION][][];
            for (int i = SCAN_DIMENSION; --i >= 0; ) {
                grid[i] = new CubeType[SCAN_DIMENSION][];
                for (int j = grid[i].length; --j >= 0; ) {
                    grid[i][j] = new CubeType[SCAN_DIMENSION];
                    for (int k = grid[i][j].length; --k >= 0; grid[i][j][k] = CubeType.Air);
                }
            }
            for (final var cube : cubes) {
                cube.setType(grid, CubeType.Lava);
            }
            this.grid = grid;
            this.cubes = cubes;
        }

        public int surfaceArea(final CubeType element) {
            int result = 0;
            for (final var cube : getCubes()) {
                result += exposedFaces(cube, element);
            }
            return result;
        }

        public void immerse() {
            final var grid = getGrid();
            final var queue = new ArrayDeque<Cube>();
            final var encountered = new HashSet<Cube>();
            final var origin = new Cube(0, 0, 0);
            encountered.add(origin);
            queue.add(origin);

            while (!queue.isEmpty()) {
                final var cube = queue.remove();
                for (final var neighbour : neighbours(cube).stream().filter(neighbour -> neighbour.getType(grid) == CubeType.Air).toList()) {
                    if (!encountered.contains(neighbour)) {
                        encountered.add(neighbour);
                        queue.add(neighbour);
                    }
                }
                cube.setType(grid, CubeType.Water);
            }
        }

        protected Collection<? extends Cube> neighbours(final Cube cube) {
            final var result = new HashSet<Cube>();
            final var x = cube.x();
            final var y = cube.y();
            final var z = cube.z();
            if (x > 0) {
                result.add(new Cube(x - 1, y, z));
            }
            if (x < SCAN_DIMENSION - 1) {
                result.add(new Cube(x + 1, y, z));
            }
            if (y > 0) {
                result.add(new Cube(x, y - 1, z));
            }
            if (y < SCAN_DIMENSION - 1) {
                result.add(new Cube(x, y + 1, z));
            }
            if (z > 0) {
                result.add(new Cube(x, y, z - 1));
            }
            if (z < SCAN_DIMENSION - 1) {
                result.add(new Cube(x, y, z + 1));
            }
            return Collections.unmodifiableSet(result);
        }

        public int exposedFaces(final Cube cube, final CubeType element) {
            final int x = cube.x();
            final int y = cube.y();
            final int z = cube.z();
            final var grid = getGrid();

            int result = 0;
            if (grid[x + 1][y][z] == element) {
                result++;
            }
            if (x <= 0 || grid[x - 1][y][z] == element) {
                result++;
            }
            if (grid[x][y + 1][z] == element) {
                result++;
            }
            if (y == 0 || grid[x][y - 1][z] == element) {
                result++;
            }
            if (grid[x][y][z + 1] == element) {
                result++;
            }
            if (z == 0 || grid[x][y][z - 1] == element) {
                result++;
            }
            return result;
        }

        public Collection<? extends Cube> getCubes() {
            return cubes;
        }

        public CubeType[][][] getGrid() {
            return grid;
        }
    }

}
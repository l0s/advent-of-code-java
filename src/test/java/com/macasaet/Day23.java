package com.macasaet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * --- Day 23: Unstable Diffusion ---
 * <a href="https://adventofcode.com/2022/day/23">https://adventofcode.com/2022/day/23</a>
 */
public class Day23 {

    record Coordinate(int x, int y) {
        public boolean hasElf(final Map<Integer, Map<Integer, Boolean>> grid) {
            return grid.getOrDefault(x(), Collections.emptyMap()).getOrDefault(y(), false);
        }

        public Set<Coordinate> neighbours() {
            final var neighbours = new HashSet<Coordinate>(8);
            for (int i = x() - 1; i <= x() + 1; i++) {
                for (int j = y() - 1; j <= y() + 1; j++) {
                    if (i == x() && j == y()) {
                        continue;
                    }
                    neighbours.add(new Coordinate(i, j));
                }
            }
            return Collections.unmodifiableSet(neighbours);
        }
    }

    enum Direction {
        North {
            public Set<Coordinate> relativeCoordinates(Coordinate reference) {
                return Set.of(new Coordinate(reference.x() - 1, reference.y() - 1),
                        adjacent(reference),
                        new Coordinate(reference.x() - 1, reference.y() + 1));
            }

            public Coordinate adjacent(Coordinate reference) {
                return new Coordinate(reference.x() - 1, reference.y());
            }
        },
        East {
            public Set<Coordinate> relativeCoordinates(Coordinate reference) {
                return Set.of(new Coordinate(reference.x() - 1, reference.y() + 1),
                        adjacent(reference),
                        new Coordinate(reference.x() + 1, reference.y() + 1));
            }

            public Coordinate adjacent(Coordinate reference) {
                return new Coordinate(reference.x(), reference.y() + 1);
            }
        },
        South {
            public Set<Coordinate> relativeCoordinates(Coordinate reference) {
                return Set.of(new Coordinate(reference.x() + 1, reference.y() - 1),
                        adjacent(reference),
                        new Coordinate(reference.x() + 1, reference.y() + 1));
            }

            public Coordinate adjacent(Coordinate reference) {
                return new Coordinate(reference.x() + 1, reference.y());
            }
        },
        West {
            public Set<Coordinate> relativeCoordinates(Coordinate reference) {
                return Set.of(new Coordinate(reference.x() - 1, reference.y() - 1),
                        adjacent(reference),
                        new Coordinate(reference.x() + 1, reference.y() - 1));
            }

            public Coordinate adjacent(Coordinate reference) {
                return new Coordinate(reference.x(), reference.y() - 1);
            }
        };

        public abstract Set<Coordinate> relativeCoordinates(Coordinate reference);

        public abstract Coordinate adjacent(Coordinate reference);
    }

    public static class Crater {

        private final Map<Integer, Map<Integer, Boolean>> grid;
        private int minX;
        private int maxX;
        private int minY;
        private int maxY;
        private List<Direction> movementPriority =
                new ArrayList<>(Arrays.asList(Direction.North, Direction.South, Direction.West, Direction.East));

        public Crater(final Map<Integer, Map<Integer, Boolean>> grid, int minX, int maxX, int minY, int maxY) {
            this.grid = grid;
            setMinX(minX);
            setMinY(minY);
            setMaxX(maxX);
            setMaxY(maxY);
        }

        public int round() {
            final var destinations = new HashMap<Coordinate, Set<Coordinate>>();
            final var moves = new HashMap<Coordinate, Coordinate>();
            // first half of round: planning phase
            for (int i = getMinX(); i <= getMaxX(); i++) {
                final var row = getGrid().get(i);
                for (int j = getMinY(); j <= getMaxY(); j++) {
                    final var from = new Coordinate(i, j);
                    if (row.getOrDefault(j, false)) {
                        // determine destination
                        final var hasNeighbour = from.neighbours()
                                .stream()
                                .anyMatch(c -> getGrid().getOrDefault(c.x(), Collections.emptyMap())
                                        .getOrDefault(c.y(), false));
                        if (!hasNeighbour) {
                            // "If no other Elves are in one of those eight positions, the Elf does not do anything
                            // during this round."
                            continue;
                        }
                        for (final var direction : getMovementPriority()) {
                            final var clear = direction.relativeCoordinates(from)
                                    .stream()
                                    .noneMatch(neighbour -> neighbour.hasElf(getGrid()));
                            if (clear) {
                                final var to = direction.adjacent(from);
                                destinations.computeIfAbsent(to, _row -> new HashSet<>()).add(from);
                                moves.put(from, to);
                                break;
                            }
                        }
                    }
                }
            }

            // second half of round: movement phase
            for (final var move : moves.entrySet()) {
                final var from = move.getKey();
                final var to = move.getValue();
                // "each Elf moves to their proposed destination tile if they were the only Elf to propose moving to
                // that position. If two or more Elves propose moving to the same position, none of those Elves move."
                if (destinations.get(to).size() == 1) {
                    getGrid().computeIfAbsent(to.x(), _row -> new HashMap<>()).put(to.y(), true);
                    getGrid().get(from.x()).put(from.y(), false);
                    setMinX(Math.min(getMinX(), to.x()));
                    setMaxX(Math.max(getMaxX(), to.x()));
                    setMinY(Math.min(getMinY(), to.y()));
                    setMaxY(Math.max(getMaxY(), to.y()));
                }
            }
            // prune edges
            // minX
            final var highestRow = getGrid().get(getMinX());
            if (highestRow.values().stream().noneMatch(hasElf -> hasElf)) {
                getGrid().remove(getMinX());
                setMinX(getMinX() + 1);
            }
            // maxX
            final var lowestRow = getGrid().get(getMaxX());
            if (lowestRow.values().stream().noneMatch(hasElf -> hasElf)) {
                getGrid().remove(getMaxX());
                setMaxX(getMaxX() - 1);
            }
            // minY
            if (getGrid().values().stream().map(row -> row.get(getMinY())).noneMatch(hasElf -> hasElf != null && hasElf)) {
                for (final var row : getGrid().values()) {
                    row.remove(getMinY());
                }
                setMinY(getMinY() + 1);
            }
            // maxY
            if (getGrid().values().stream().map(row -> row.get(getMaxY())).noneMatch(hasElf -> hasElf != null && hasElf)) {
                for (final var row : getGrid().values()) {
                    row.remove(getMaxY());
                }
                setMaxY(getMaxY() + 1);
            }

            // "Finally, at the end of the round, the first direction the Elves considered is moved to the end of the
            // list of directions."
            final var previousFirst = getMovementPriority().remove(0);
            getMovementPriority().add(previousFirst);
            return moves.size();
        }

        public int countEmptyGroundTiles() {
            int result = 0;
            for (int i = getMinX(); i <= getMaxX(); i++) {
                final var row = getGrid().getOrDefault(i, Collections.emptyMap());
                for (int j = getMinY(); j <= getMaxY(); j++) {
                    if (!row.getOrDefault(j, false)) {
                        result++;
                    }
                }
            }
            return result;
        }

        public static Crater fromString(final String block) {
            final var lines = block.split("\n");
            final Map<Integer, Map<Integer, Boolean>> grid = new HashMap<>(lines.length);
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (int i = lines.length; --i >= 0; ) {
                final var line = lines[i];
                final var chars = line.toCharArray();
                final Map<Integer, Boolean> row = new HashMap<>(chars.length);
                boolean rowHasElf = false;
                for (int j = chars.length; --j >= 0; ) {
                    if (chars[j] == '#') {
                        minY = Math.min(minY, j);
                        maxY = Math.max(maxY, j);
                        row.put(j, true);
                        rowHasElf |= true;
                    } else {
                        row.put(j, false);
                    }
                }
                grid.put(i, row);
                if (rowHasElf) {
                    minX = Math.min(minX, i);
                    maxX = Math.max(maxX, i);
                }
            }
            return new Crater(grid, minX, maxX, minY, maxY);
        }

        public String toString() {
            final var builder = new StringBuilder();
            builder.append("X: [").append(getMinX()).append(", ").append(getMaxX()).append("]\n");
            builder.append("Y: [").append(getMinY()).append(", ").append(getMaxY()).append("]\n");
            builder.append("Movement priority: ").append(getMovementPriority()).append("\n");
            appendGrid(builder);
            return builder.toString();
        }

        protected void appendGrid(final StringBuilder builder) {
            for (int i = getMinX(); i <= getMaxX(); i++) {
                final var row = getGrid().getOrDefault(i, Collections.emptyMap());
                for (int j = getMinY(); j <= getMaxY(); j++) {
                    final var c = row.getOrDefault(j, false)
                            ? '#'
                            : '.';
                    builder.append(c);
                }
                builder.append('\n');
            }
        }

        protected Map<Integer, Map<Integer, Boolean>> getGrid() {
            return grid;
        }

        protected int getMinX() {
            return minX;
        }

        protected void setMinX(int minX) {
            this.minX = minX;
        }

        protected int getMaxX() {
            return maxX;
        }

        protected void setMaxX(int maxX) {
            this.maxX = maxX;
        }

        protected int getMinY() {
            return minY;
        }

        protected void setMinY(int minY) {
            this.minY = minY;
        }

        protected int getMaxY() {
            return maxY;
        }

        protected void setMaxY(int maxY) {
            this.maxY = maxY;
        }

        protected List<Direction> getMovementPriority() {
            return movementPriority;
        }

        protected void setMovementPriority(List<Direction> movementPriority) {
            this.movementPriority = movementPriority;
        }
    }

    protected static Crater getInput() {
        final var lines = StreamSupport.stream(new LineSpliterator("day-23.txt"), false)
                .collect(Collectors.joining("\n"));
        return Crater.fromString(lines);
    }

    @Test
    public final void part1() {
        final var crater = getInput();
        for (int i = 10; --i >= 0; crater.round()) ;
        final var result = crater.countEmptyGroundTiles();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void testRound1() {
        // given
        final var block = """
                ##
                #.
                ..
                ##
                """;
        final var crater = Crater.fromString(block);

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ##
                ..
                #.
                .#
                #.
                """, result);
    }

    @Test
    public final void testRound2() {
        // given
        final var block = """
                ##
                ..
                #.
                .#
                #.
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.South, Direction.West, Direction.East, Direction.North)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                .##.
                #...
                ...#
                ....
                .#..
                """, result);
    }

    @Test
    public final void testRound3() {
        // given
        final var block = """
                .##.
                #...
                ...#
                ....
                .#..
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.West, Direction.East, Direction.North, Direction.South)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ..#..
                ....#
                #....
                ....#
                .....
                ..#..
                """, result);
    }

    @Test
    public final void testLargerRound1() {
        // given
        final var block = """
                .......#......
                .....###.#....
                ...#...#.#....
                ....#...##....
                ...#.###......
                ...##.#.##....
                ....#..#......
                """;
        final var crater = Crater.fromString(block);

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                .....#...
                ...#...#.
                .#..#.#..
                .....#..#
                ..#.#.##.
                #..#.#...
                #.#.#.##.
                .........
                ..#..#...
                """, result);
    }

    @Test
    public final void testLargerRound2() {
        // given
        final var block = """
                .....#...
                ...#...#.
                .#..#.#..
                .....#..#
                ..#.#.##.
                #..#.#...
                #.#.#.##.
                .........
                ..#..#...
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.South, Direction.West, Direction.East, Direction.North)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ......#....
                ...#.....#.
                ..#..#.#...
                ......#...#
                ..#..#.#...
                #...#.#.#..
                ...........
                .#.#.#.##..
                ...#..#....
                """, result);
    }

    @Test
    public final void testLargerRound3() {
        // given
        final var block = """
                ......#....
                ...#.....#.
                ..#..#.#...
                ......#...#
                ..#..#.#...
                #...#.#.#..
                ...........
                .#.#.#.##..
                ...#..#....
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.West, Direction.East, Direction.North, Direction.South)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ......#....
                ....#....#.
                .#..#...#..
                ......#...#
                ..#..#.#...
                #..#.....#.
                ......##...
                .##.#....#.
                ..#........
                ......#....
                """, result);
    }

    @Test
    public final void testLargerRound4() {
        // given
        final var block = """
                ......#....
                ....#....#.
                .#..#...#..
                ......#...#
                ..#..#.#...
                #..#.....#.
                ......##...
                .##.#....#.
                ..#........
                ......#....
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.East, Direction.North, Direction.South, Direction.West)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ......#....
                .....#....#
                .#...##....
                ..#.....#.#
                ........#..
                #...###..#.
                .#......#..
                ...##....#.
                ...#.......
                ......#....
                """, result);
    }

    @Test
    public final void testLargerRound5() {
        // given
        final var block = """
                ......#....
                .....#....#
                .#...##....
                ..#.....#.#
                ........#..
                #...###..#.
                .#......#..
                ...##....#.
                ...#.......
                ......#....
                """;
        final var crater = Crater.fromString(block);
        crater.setMovementPriority(new ArrayList<>(Arrays.asList(Direction.North, Direction.South, Direction.West, Direction.East)));

        // when
        crater.round();

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ......#....
                ...........
                .#..#.....#
                ........#..
                .....##...#
                #.#.####...
                ..........#
                ...##..#...
                .#.........
                .........#.
                ...#..#....
                """, result);
    }

    @Test
    public final void testLargerRounds() {
        // given
        final var block = """
                .......#......
                .....###.#....
                ...#...#.#....
                ....#...##....
                ...#.###......
                ...##.#.##....
                ....#..#......
                """;
        final var crater = Crater.fromString(block);

        // when
        for (int i = 10; --i >= 0; crater.round()) ;

        // then
        final var builder = new StringBuilder();
        crater.appendGrid(builder);
        final var result = builder.toString();
        Assertions.assertEquals("""
                ......#.....
                ..........#.
                .#.#..#.....
                .....#......
                ..#.....#..#
                #......##...
                ....##......
                .#........#.
                ...#.#..#...
                ............
                ...#..#..#..
                """, result);
        Assertions.assertEquals(110, crater.countEmptyGroundTiles());
    }

    @Test
    public final void part2() {
        final var crater = getInput();
        int rounds = 0;
        while (true) {
            final var moves = crater.round();
            rounds++;
            if (moves == 0) {
                break;
            }
        }
        System.out.println("Part 2: " + rounds);
    }

}
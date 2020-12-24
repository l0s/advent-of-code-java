package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Day24 {

    public static void main(final String[] args) throws IOException {
        final Address origin = new Address(0, 0, 0);
        final var lobbyFloor = new HashMap<Address, Integer>();
        try (var spliterator = new LineSpliterator(Day22.class.getResourceAsStream("/day-24-input.txt"))) {
            StreamSupport.stream(spliterator, false)
                    .map(String::toCharArray)
                    .map(array -> {
                        int i = 0;
                        var directions = new ArrayList<Direction>(array.length);
                        while (i < array.length) {
                            if (i < array.length - 1) { // check if there are at least 2 chars available
                                final var twoLetterDirection = "" + array[i] + array[i + 1];
                                // check if two chars is a valid direction
                                if ("se".equalsIgnoreCase(twoLetterDirection)
                                        || "sw".equalsIgnoreCase(twoLetterDirection)
                                        || "nw".equalsIgnoreCase(twoLetterDirection)
                                        || "ne".equalsIgnoreCase(twoLetterDirection)) {
                                    directions.add(Direction.forAbbreviation(twoLetterDirection));
                                    i = i + 2;
                                    continue;
                                }
                            }
                            final var oneLetterDirection = "" + array[i];
                            if ("e".equalsIgnoreCase(oneLetterDirection) || "w".equalsIgnoreCase(oneLetterDirection)) {
                                directions.add(Direction.forAbbreviation(oneLetterDirection));
                                i = i + 1;
                                continue;
                            }
                            throw new IllegalArgumentException("Invalid direction: " + oneLetterDirection);
                        }
                        return Collections.unmodifiableList(directions);
                    }).map(directions -> {
                Address cursor = origin;
                for (final var direction : directions) {
                    cursor = direction.travel(cursor);
                }
                return cursor;
            }).forEach(address -> lobbyFloor.merge(address, 1, (old, def) -> old + 1));
        }
        final int blackTiles = lobbyFloor.values().stream().mapToInt(count -> count % 2).sum();
        System.out.println("Part 1: " + blackTiles);

        for (int i = 1; i <= 100; i++) {
            final var tasks = lobbyFloor.entrySet().stream().flatMap(entry -> {
                // get the mapped tile
                // as well as unmapped (white) adjacent tiles
                final var from = entry.getKey();
                return Stream.concat(Stream.of(entry), Arrays.stream(Direction.values()).flatMap(direction -> {
                    final var neighbour = direction.travel(from);
                    if (!lobbyFloor.containsKey(neighbour)) {
                        // neighbour has never been visited, create a virtual entry for them
                        return Stream.of(new Entry<Address, Integer>() {
                            public Address getKey() {
                                return neighbour;
                            }

                            public Integer getValue() {
                                return 0;
                            }

                            public Integer setValue(Integer value) {
                                throw new UnsupportedOperationException();
                            }
                        });
                    }
                    return Stream.empty();
                }));
            }).map(entry -> { // NB: this might not be a real tile
                final var address = entry.getKey();
                final int adjacentBlackTiles = Arrays.stream(Direction.values())
                        .map(direction -> direction.travel(address))
                        .mapToInt(neighbour -> {
                            final Integer neighbouringCount = lobbyFloor.get(neighbour);
                            return neighbouringCount != null && neighbouringCount % 2 == 1 ? 1 : 0;
                        }).sum();
                if (entry.getValue() % 2 == 1 && (adjacentBlackTiles == 0 || adjacentBlackTiles > 2)) {
                    // Any black tile with zero or more than 2 black tiles immediately adjacent to it is flipped to white.
                    return (Runnable) () -> lobbyFloor.put(address, 0);
                } else if (entry.getValue() % 2 == 0 && adjacentBlackTiles == 2) {
                    // Any white tile with exactly 2 black tiles immediately adjacent to it is flipped to black.
                    return (Runnable) () -> lobbyFloor.put(address, 1);
                }
                return (Runnable) () -> {
                };
            }).collect(Collectors.toUnmodifiableList());
            for (final var task : tasks) {
                task.run();
            }
        }
        final int count = lobbyFloor.values().stream().mapToInt(value -> value % 2).sum();
        System.out.println("Part 2: " + count);
    }

    public enum Direction {
        EAST("e", 1, -1, 0),
        SOUTH_EAST("se", 0, -1, 1),
        SOUTH_WEST("sw", -1, 0, 1),
        WEST("w", -1, 1, 0),
        NORTH_WEST("nw", 0, 1, -1),
        NORTH_EAST("ne", 1, 0, -1);

        private final String abbreviation;
        private final int xOffset;
        private final int yOffset;
        private final int zOffset;

        Direction(final String abbreviation, final int xOffset, final int yOffset, final int zOffset) {
            this.abbreviation = abbreviation;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
        }

        public static Direction forAbbreviation(final String abbreviation) {
            for (final var candidate : values()) {
                if (candidate.abbreviation.equalsIgnoreCase(abbreviation)) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException("Invalid direction: " + abbreviation);
        }

        public Address travel(final Address from) {
            return new Address(from.x + xOffset, from.y + yOffset, from.z + zOffset);
        }

    }

    public static class Address {

        private final int x;
        private final int y;
        private final int z;

        public Address(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int hashCode() {
            int retval = 0;
            retval = 31 * retval + x;
            retval = 31 * retval + y;
            retval = 31 * retval + z;
            return retval;
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            } else if (o == null) {
                return false;
            }
            try {
                final Address other = (Address) o;
                return x == other.x && y == other.y && z == other.z;
            } catch (final ClassCastException cce) {
                return false;
            }
        }

    }

}
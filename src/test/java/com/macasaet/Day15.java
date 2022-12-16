package com.macasaet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.stream.StreamSupport;

/**
 * --- Day 15: Beacon Exclusion Zone ---
 * <a href="https://adventofcode.com/2022/day/15">https://adventofcode.com/2022/day/15</a>
 */
public class Day15 {

    record Coordinate(int x, int y) {
        static Coordinate parse(final String string) {
            final var components = string.split(", ");
            return new Coordinate(Integer.parseInt(components[1].replaceAll("^y=", "")),
                    Integer.parseInt(components[0].replaceAll("^x=", "")));
        }

        Map<Integer, Item> getRow(final Map<Integer, Map<Integer, Item>> grid) {
            return grid.computeIfAbsent(x(), ignored -> new HashMap<>());
        }

        int distanceTo(final Coordinate other) {
            return Math.abs(x() - other.x()) + Math.abs(y() - other.y());
        }
    }

    public record Sensor(Coordinate location, Coordinate beaconLocation) {
        public static Sensor parse(final String line) {
            final var location = Coordinate.parse(line.replaceAll("^Sensor at ", "").replaceAll(": closest beacon is at .*$", ""));
            final var beaconLocation = Coordinate.parse(line.replaceAll("^.*: closest beacon is at ", ""));
            return new Sensor(location, beaconLocation);
        }

        void setSensor(final Map<Integer, Map<Integer, Item>> grid, IntPredicate includeRow, IntPredicate includeColumn) {
            if(includeRow.test(location().x()) && includeColumn.test(location().y())) {
                location().getRow(grid).put(location().y(), Item.SENSOR);
            }
        }

        void setBeacon(final Map<Integer, Map<Integer, Item>> grid, IntPredicate includeRow, IntPredicate includeColumn) {
            if(includeRow.test(beaconLocation().x()) && includeColumn.test(beaconLocation().y())) {
                beaconLocation().getRow(grid).put(beaconLocation().y(), Item.BEACON);
            }
        }

        void setCoverageArea(final Map<Integer, Map<Integer, Item>> grid, IntPredicate includeRow, IntPredicate includeColumn) {
            final var distance = distanceToBeacon();
            final var x = location().x();
            final var y = location().y();

            for(int i = 0; i <= distance; i++ ) {
                if(!includeRow.test(x + i) && !includeRow.test(x - i)) {
                    continue;
                }
                final var lowerRow = includeRow.test(x + i)
                        ? grid.computeIfAbsent(x + i, ignored -> new HashMap<>())
                        : new HashMap<Integer, Item>();
                final var upperRow = includeRow.test(x - i)
                        ? grid.computeIfAbsent(x - i, ignored -> new HashMap<>())
                        : new HashMap<Integer, Item>();
                for(int j = 0; j <= distance - i; j++ ) {
                    if(includeColumn.test(y + j)) {
                        // SE
                        lowerRow.putIfAbsent(y + j, Item.COVERED);
                        // NE
                        upperRow.putIfAbsent(y + j, Item.COVERED);
                    }
                    if(includeColumn.test(y - j)) {
                        // SW
                        lowerRow.putIfAbsent(y - j, Item.COVERED);

                        // NW
                        upperRow.putIfAbsent(y - j, Item.COVERED);
                    }
                }
            }
        }

        int distanceToBeacon() {
            return location().distanceTo(beaconLocation());
        }
    }

    enum Item {
        SENSOR,
        BEACON,
        COVERED
    }
    public record CaveMap(Map<Integer, Map<Integer, Item>> grid, int minX, int maxX, int minY, int maxY) {
        public int countCoveredCellsInRow(final int x) {
            final var row = grid().getOrDefault(x, Collections.emptyMap());
            int result = 0;
            for(int j = minY(); j <= maxY(); j++) {
                final var cell = row.get(j);
                if(cell != null && cell != Item.BEACON) {
                    result++;
                }
            }
            return result;
        }

        public static CaveMap fromSensors(final Iterable<? extends Sensor> sensors, IntPredicate includeRow, IntPredicate includeColumn) {
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;
            final var grid = new HashMap<Integer, Map<Integer, Item>>();
            for(final var sensor : sensors) {
                minX = Math.min(minX, sensor.location().x() - sensor.distanceToBeacon());
                maxX = Math.max(maxX, sensor.location().x() + sensor.distanceToBeacon());
                minY = Math.min(minY, sensor.location().y() - sensor.distanceToBeacon());
                maxY = Math.max(maxY, sensor.location().y() + sensor.distanceToBeacon());

                sensor.setCoverageArea(grid, includeRow, includeColumn);
                sensor.setBeacon(grid, includeRow, includeColumn);
                sensor.setSensor(grid, includeRow, includeColumn);
            }

            return new CaveMap(grid, minX, maxX, minY, maxY);
        }

        public String toString() {
            final var builder = new StringBuilder();
            for(int i = minX(); i <= maxX(); i++) {
                builder.append(i).append('\t');
                final var row = grid().getOrDefault(i, Collections.emptyMap());
                for(int j = minY(); j <= maxY(); j++) {
                    final var item = row.get(j);
                    final var marker = item == null
                            ? '.'
                            : item == Item.BEACON
                            ? 'B'
                            : item == Item.SENSOR
                            ? 'S'
                            : '#';
                    builder.append(marker);
                }
                builder.append('\n');
            }
            return builder.toString();
        }
    }

    protected CaveMap getInput(final IntPredicate includeRow, IntPredicate includeColumn) {
        final var sensors = getSensors();
        return CaveMap.fromSensors(sensors, includeRow, includeColumn);
    }

    protected static List<Sensor> getSensors() {
        return StreamSupport.stream(new LineSpliterator("day-15.txt"), false)
                .map(Sensor::parse)
                .toList();
    }

    @Test
    public final void part1() {
        final int rowOfInterest = 2_000_000;
        final var map = getInput(row -> row == rowOfInterest, _column -> true);
        final var result = map.countCoveredCellsInRow(rowOfInterest);

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final int max = 4_000_000;
        final var sensors = getSensors();
        for(final var sensor : sensors) {
            final var x = sensor.location().x();
            final var y = sensor.location().y();
            final var reach = sensor.distanceToBeacon();
            // Find all the points just outside this sensor's reach
            for(int horizontalOffset = 0; horizontalOffset <= reach + 1; horizontalOffset++) {
                final var verticalOffset = reach + 1 - horizontalOffset;
                Assertions.assertEquals(horizontalOffset + verticalOffset, reach + 1);
                for(final var candidate : Arrays.asList(new Coordinate(x + verticalOffset, y + horizontalOffset), // SE
                        new Coordinate(x + verticalOffset, y - horizontalOffset), // SW
                        new Coordinate(x - verticalOffset, y + horizontalOffset), // NE
                        new Coordinate(x - verticalOffset, y - horizontalOffset))) { // NW
                    if(candidate.x() < 0 || candidate.y() < 0 || candidate.x() > max || candidate.y() > max) {
                        continue;
                    }
                    Assertions.assertTrue(candidate.distanceTo(sensor.location()) > sensor.distanceToBeacon());
                    // Check if the point is also outside the reach of every other sensor
                    if(sensors.stream().allMatch(other -> candidate.distanceTo(other.location()) > other.distanceToBeacon())) {
                        final long result = (long)candidate.y() * 4_000_000l + (long)candidate.x();
                        System.out.println("Part 2: " + result);
                        return;
                    }
                }
            }
        }
        throw new IllegalStateException("No uncovered point found");
    }

}
package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 9: Rope Bridge ---
 * <a href="https://adventofcode.com/2022/day/9">https://adventofcode.com/2022/day/9</a>
 */
public class Day09 {

    record Coordinate(int x, int y) {

        public int distance(final Coordinate other) {
            return (int)Math.sqrt(Math.pow((double)x() - (double)other.x(), 2.0) + Math.pow((double)y() - (double)other.y(), 2.0));
        }

        public Coordinate step(final int xDistance, final int yDistance) {
            return new Coordinate(x() + xDistance, y + yDistance);
        }
        public Coordinate stepTowards(final Coordinate leader) {
            final var xDistance = Integer.compare(leader.x(), x());
            final var yDistance = Integer.compare(leader.y(), y());
            return step(xDistance, yDistance);
        }
    }

    public static class Rope {

        Coordinate[] knotCoordinates;

        final SortedMap<Integer, Set<Integer>> visited = new TreeMap<>();

        public Rope(final int knots) {
            knotCoordinates = new Coordinate[knots];
            for(int i = knots; --i >= 0; knotCoordinates[i] = new Coordinate(0, 0));
            visited.computeIfAbsent(0, (key) -> new TreeSet<>()).add(0);
        }

        public int countVisited() {
            int result = 0;
            for( final var map : visited.values() ) {
                result += map.size();
            }
            return result;
        }

        public void process(final Instruction instruction) {
            final int xStep = instruction.direction().xStep();
            final int yStep = instruction.direction().yStep();

            for(int i = instruction.distance(); --i >= 0; ) {
                knotCoordinates[0] = knotCoordinates[0].step(xStep, yStep);
                for(int j = 1; j < knotCoordinates.length; j++) {
                    moveKnot(j);
                }
            }
        }

        protected void moveKnot(int knotIndex) {
            if(knotIndex <= 0) {
                throw new IllegalArgumentException("Cannot move head");
            }
            final var leader = knotCoordinates[knotIndex - 1];
            var follower = knotCoordinates[knotIndex];

            if(leader.equals(follower)) {
                return;
            } else if (leader.distance(follower) <= 1) {
                return;
            }

            follower = follower.stepTowards(leader);
            knotCoordinates[knotIndex] = follower;

            if(knotIndex == knotCoordinates.length - 1) {
                visited.computeIfAbsent(follower.x(), (key) -> new TreeSet<>()).add(follower.y());
            }
        }

    }

    enum Direction {
        Up {
            int xStep() {
                return -1;
            }
            int yStep() {
                return 0;
            }
        },
        Down {
            int xStep() {
                return 1;
            }
            int yStep() {
                return 0;
            }
        },
        Left {
            int xStep() {
                return 0;
            }
            int yStep() {
                return -1;
            }
        },
        Right {
            int xStep() {
                return 0;
            }
            int yStep() {
                return 1;
            }
        };

        abstract int xStep();
        abstract int yStep();

        static Direction parse(final String string) {
            return switch(string.trim()) {
                case "U" -> Up;
                case "D" -> Down;
                case "L" -> Left;
                case "R" -> Right;
                default -> throw new IllegalArgumentException("Invalid direction: " + string);
            };
        }
    }

    record Instruction(Direction direction, int distance) {
        static Instruction parse(final String string) {
            final var components = string.split(" ");
            final var direction = Direction.parse(components[0]);
            final var distance = Integer.parseInt(components[1]);
            return new Instruction(direction, distance);
        }
    }

    protected Stream<Instruction> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-09.txt"),
                        false)
                .map(Instruction::parse);
    }

    @Test
    public final void part1() {
        final var rope = new Rope(2);
        getInput().forEach(rope::process);
        final var result = rope.countVisited();
        System.out.println("Part 1: " + result);
        // NOT 7017
    }

    @Test
    public final void part2() {
        final var rope = new Rope(10);
        getInput().forEach(rope::process);
        final var result = rope.countVisited();
        System.out.println("Part 2: " + result);
    }

}
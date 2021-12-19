package com.macasaet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 19: Beacon Scanner ---
 */
public class Day19 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-19.txt"),
                        false);
    }

    protected List<Scanner> getScanners() {
        final var list = getInput().toList();
        final var result = new ArrayList<Scanner>();
        var observations = new HashSet<Position>();
        int id = -1;
        for (final var line : list) {
            if (line.startsWith("--- scanner ")) {
                id = Integer.parseInt(line
                        .replaceFirst("^--- scanner ", "")
                        .replaceFirst(" ---$", ""));
            } else if (!line.isBlank()) {
                observations.add(Position.parse(line));
            } else { // line is blank
                result.add(new Scanner(id, Collections.unmodifiableSet(observations)));
                observations = new HashSet<>();
                id = -1;
            }
        }
        result.add(new Scanner(id, Collections.unmodifiableSet(observations)));
        return Collections.unmodifiableList(result);
    }

    public enum Direction {
        POSITIVE_X {
            public Position face(Position position) {
                return position;
            }
        },
        NEGATIVE_X {
            public Position face(Position position) {
                return new Position(-position.x(), position.y(), -position.z());
            }
        },
        POSITIVE_Y {
            public Position face(Position position) {
                return new Position(position.y(), -position.x(), position.z());
            }
        },
        NEGATIVE_Y {
            public Position face(Position position) {
                return new Position(-position.y(), position.x(), position.z());
            }
        },
        POSITIVE_Z {
            public Position face(Position position) {
                return new Position(position.z(), position.y(), -position.x());
            }
        },
        NEGATIVE_Z {
            public Position face(Position position) {
                return new Position(-position.z(), position.y(), position.x());
            }
        };

        public abstract Position face(final Position position);

    }

    public enum Rotation {
        r0 {
            public Position rotate(final Position position) {
                return position;
            }
        },
        r90 {
            public Position rotate(Position position) {
                return new Position(position.x(), -position.z(), position.y());
            }
        },
        r180 {
            public Position rotate(Position position) {
                return new Position(position.x(), -position.y(), -position.z());
            }
        },
        r270 {
            public Position rotate(Position position) {
                return new Position(position.x(), position.z(), -position.y());
            }
        };

        public abstract Position rotate(final Position position);
    }

    public record Transformation(Direction direction, Rotation rotation) {
        /**
         * Look at a position from a specific orientation
         *
         * @param position a position relative to one point of view
         * @return the same position relative to a different point of view
         */
        public Position reorient(final Position position) {
            return rotation.rotate(direction.face(position));
        }

    }

    public record Position(int x, int y, int z) {
        public static Position parse(final String line) {
            final var components = line.split(",");
            return new Position(Integer.parseInt(components[0]),
                    Integer.parseInt(components[1]),
                    Integer.parseInt(components[2]));
        }

        public Position plus(Position amount) {
            return new Position(x() + amount.x(), y() + amount.y(), z() + amount.z());
        }

        public Position minus(final Position other) {
            return new Position(x() - other.x(), y() - other.y(), z() - other.z());
        }
    }

    public interface OverlapResult {
    }

    public record Overlap(Position distance, Transformation transformation,
                          Set<Position> overlappingBeacons) implements OverlapResult {
    }

    public record None() implements OverlapResult {
    }

    public record Scanner(int id, Set<Position> observations) {

        public OverlapResult getOverlappingBeacons(final Scanner other) {
            for (final var direction : Direction.values()) {
                for (final var rotation : Rotation.values()) {
                    final var transformation = new Transformation(direction, rotation);
                    final var distances = observations().stream()
                            .flatMap(a -> other.observations()
                                    .stream()
                                    .map(transformation::reorient)
                                    .map(a::minus))
                            .collect(Collectors.toList());
                    for (final var offset : distances) {
                        final var intersection = other.observations()
                                .stream()
                                .map(transformation::reorient)
                                .map(observation -> observation.plus(offset))
                                .filter(observations()::contains)
                                .collect(Collectors.toUnmodifiableSet());
                        if (intersection.size() >= 12) {
                            return new Overlap(offset, transformation, intersection);
                        }
                    }
                }
            }
            return new None();
        }

    }

    @Nested
    public class ScannerTest {
        @Test
        public final void testOverlapWithOrigin() {
            // given
            final var scanner0Observations = """
                    404,-588,-901
                    528,-643,409
                    -838,591,734
                    390,-675,-793
                    -537,-823,-458
                    -485,-357,347
                    -345,-311,381
                    -661,-816,-575
                    -876,649,763
                    -618,-824,-621
                    553,345,-567
                    474,580,667
                    -447,-329,318
                    -584,868,-557
                    544,-627,-890
                    564,392,-477
                    455,729,728
                    -892,524,684
                    -689,845,-530
                    423,-701,434
                    7,-33,-71
                    630,319,-379
                    443,580,662
                    -789,900,-551
                    459,-707,401
                    """;
            final var scanner1Observations = """
                    686,422,578
                    605,423,415
                    515,917,-361
                    -336,658,858
                    95,138,22
                    -476,619,847
                    -340,-569,-846
                    567,-361,727
                    -460,603,-452
                    669,-402,600
                    729,430,532
                    -500,-761,534
                    -322,571,750
                    -466,-666,-811
                    -429,-592,574
                    -355,545,-477
                    703,-491,-529
                    -328,-685,520
                    413,935,-424
                    -391,539,-444
                    586,-435,557
                    -364,-763,-893
                    807,-499,-711
                    755,-354,-619
                    553,889,-390
                    """;
            final var scanner0 = new Scanner(0,
                    Arrays.stream(scanner0Observations.split("\n"))
                            .map(Position::parse)
                            .collect(Collectors.toUnmodifiableSet()));
            final var scanner1 = new Scanner(0,
                    Arrays.stream(scanner1Observations.split("\n"))
                            .map(Position::parse)
                            .collect(Collectors.toUnmodifiableSet()));

            // when
            final var result = scanner0.getOverlappingBeacons(scanner1);

            // then
            assertTrue(result instanceof Overlap);
            final var overlap = (Overlap) result;
            assertEquals(12, overlap.overlappingBeacons().size());
        }

        @Test
        public final void testOverlapWithNonOrigin() {
            // given
            final var scanner1Observations = """
                    686,422,578
                    605,423,415
                    515,917,-361
                    -336,658,858
                    95,138,22
                    -476,619,847
                    -340,-569,-846
                    567,-361,727
                    -460,603,-452
                    669,-402,600
                    729,430,532
                    -500,-761,534
                    -322,571,750
                    -466,-666,-811
                    -429,-592,574
                    -355,545,-477
                    703,-491,-529
                    -328,-685,520
                    413,935,-424
                    -391,539,-444
                    586,-435,557
                    -364,-763,-893
                    807,-499,-711
                    755,-354,-619
                    553,889,-390
                    """;
            final var scanner4Observations = """
                    727,592,562
                    -293,-554,779
                    441,611,-461
                    -714,465,-776
                    -743,427,-804
                    -660,-479,-426
                    832,-632,460
                    927,-485,-438
                    408,393,-506
                    466,436,-512
                    110,16,151
                    -258,-428,682
                    -393,719,612
                    -211,-452,876
                    808,-476,-593
                    -575,615,604
                    -485,667,467
                    -680,325,-822
                    -627,-443,-432
                    872,-547,-609
                    833,512,582
                    807,604,487
                    839,-516,451
                    891,-625,532
                    -652,-548,-490
                    30,-46,-14
                    """;
            final var scanner1 = new Scanner(0,
                    Arrays.stream(scanner1Observations.split("\n"))
                            .map(Position::parse)
                            .collect(Collectors.toUnmodifiableSet()));
            final var scanner4 = new Scanner(0,
                    Arrays.stream(scanner4Observations.split("\n"))
                            .map(Position::parse)
                            .collect(Collectors.toUnmodifiableSet()));

            // when
            final var result = scanner1.getOverlappingBeacons(scanner4);

            // then
            assertTrue(result instanceof Overlap);
            final var overlap = (Overlap) result;
            assertEquals(12, overlap.overlappingBeacons().size());
        }
    }

    @Test
    public final void part1() {
        final var scanners = getScanners();
        final var knownBeacons = new HashSet<Position>();
        final var origin = new Scanner(-1, knownBeacons);
        final var remaining = new ArrayList<>(scanners);
        while (!remaining.isEmpty()) {
            final var other = remaining.remove(0);
            if (knownBeacons.isEmpty()) {
                knownBeacons.addAll(other.observations());
                continue;
            }
            final var result = origin.getOverlappingBeacons(other);
            if (result instanceof final Overlap overlap) {
                knownBeacons.addAll(other.observations()
                        .stream()
                        .map(overlap.transformation()::reorient)
                        .map(observation -> observation.plus(overlap.distance()))
                        .collect(Collectors.toList()));
            } else {
                remaining.add(other);
            }
        }
        System.out.println("Part 1: " + knownBeacons.size());
    }

    @Test
    public final void part2() {
        final var scanners = getScanners();
        final var knownBeacons = new HashSet<Position>();
        final var origin = new Scanner(-1, knownBeacons);
        final var remaining = new ArrayList<>(scanners);
        final var distances = new HashSet<Position>();
        while (!remaining.isEmpty()) {
            final var other = remaining.remove(0);
            if (knownBeacons.isEmpty()) {
                knownBeacons.addAll(other.observations());
                continue;
            }
            final var result = origin.getOverlappingBeacons(other);
            if (result instanceof final Overlap overlap) {
                knownBeacons.addAll(other.observations()
                        .stream()
                        .map(overlap.transformation()::reorient)
                        .map(observation -> observation.plus(overlap.distance()))
                        .collect(Collectors.toList()));
                distances.add(overlap.distance());
            } else {
                remaining.add(other);
            }
        }
        int maxDistance = Integer.MIN_VALUE;
        for (final var x : distances) {
            for (final var y : distances) {
                final int distance = Math.abs(x.x() - y.x())
                        + Math.abs(x.y() - y.y())
                        + Math.abs(x.z() - y.z());
                maxDistance = Math.max(maxDistance, distance);
            }
        }
        System.out.println("Part 2: " + maxDistance);
    }

}
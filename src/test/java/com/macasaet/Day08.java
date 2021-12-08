package com.macasaet;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 8: Seven Segment Search ---
 */
public class Day08 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-08.txt"),
                        false);
    }

    public record Digit(List<Character> segments) {
        public Digit decode(final Map<Character, Character> map) {
            return new Digit(segments().stream()
                    .map(map::get)
                    .collect(Collectors.toUnmodifiableList()));
        }

        public int asInt() {
            if (segments().size() == 6 && hasSegments('a', 'b', 'c', 'e', 'f', 'g')) { // FIXME use on/off mechanism
                return 0;
            } else if (segments().size() == 2 && hasSegments('c', 'f')) {
                return 1;
            } else if (segments().size() == 5 && hasSegments('a', 'c', 'd', 'e', 'g')) {
                return 2;
            } else if (segments().size() == 5 && hasSegments('a', 'c', 'd', 'f', 'g')) {
                return 3;
            } else if (segments().size() == 4 && hasSegments('b', 'c', 'd', 'f')) {
                return 4;
            } else if (segments().size() == 5 && hasSegments('a', 'b', 'd', 'f', 'g')) {
                return 5;
            } else if (segments().size() == 6 && hasSegments('a', 'b', 'd', 'e', 'f', 'g')) {
                return 6;
            } else if (segments().size() == 3 && hasSegments('a', 'c', 'f')) {
                return 7;
            } else if (segments().size() == 7 && hasSegments('a', 'b', 'c', 'd', 'e', 'f', 'g')) {
                return 8;
            } else if (segments().size() == 6 && hasSegments('a', 'b', 'c', 'd', 'f', 'g')) {
                return 9;
            }
            throw new IllegalStateException("Invalid Digit: " + this);
        }

        public boolean hasSegments(final char... segments) {
            for (final var segment : segments) {
                if (!hasSegment(segment)) {
                    return false;
                }
            }
            return true;
        }

        public boolean hasSegment(final char segment) {
            return segments().contains(segment);
        }

        public static Digit parse(final String string) {
            final var array = string.toCharArray();
            final var list = new ArrayList<Character>(array.length);
            for (final var c : array) {
                list.add(c);
            }
            return new Digit(Collections.unmodifiableList(list));
        }
    }

    public static record Entry(List<Digit> uniqueSignalPatterns, List<Digit> outputValue) {

        public int decodeOutput() {
            final var map = buildDecodingMap();
            final StringBuilder builder = new StringBuilder();
            for (final var outputDigit : outputValue()) {
                final var decodedDigit = outputDigit.decode(map);
                final int digit = decodedDigit.asInt();
                builder.append(digit);
            }
            final String stringInt = builder.toString();
            return Integer.parseInt(stringInt);
        }

        protected SortedSet<Character> getDigitSegmentsWithCount(final int n) {
            return uniqueSignalPatterns().stream()
                    .filter(digit -> digit.segments().size() == n)
                    .findFirst()
                    .get()
                    .segments()
                    .stream()
                    .collect(TreeSet::new, SortedSet::add, SortedSet::addAll);
        }

        protected Set<Digit> getDigitsWithCount(final int n) { // TODO return stream
            return uniqueSignalPatterns()
                    .stream()
                    .filter(digit -> digit.segments().size() == n).collect(Collectors.toUnmodifiableSet());
        }

        public Map<Character, Character> buildDecodingMap() {
            final var encodingMap = buildEncodingMap();
            final var result = new HashMap<Character, Character>();
            for(final var entry : encodingMap.entrySet()) {
                result.put(entry.getValue(), entry.getKey());
            }
            return Collections.unmodifiableMap(result);
        }

        public Map<Character, Character> buildEncodingMap() {
            final var map = new HashMap<Character, Character>();
            final var oneSegments = getDigitSegmentsWithCount(2);
            final var sevenSegments = getDigitSegmentsWithCount(3);
            final var fourSegments = getDigitSegmentsWithCount(4);
            final var eightSegments = getDigitSegmentsWithCount(7);
            final var aMapping = sevenSegments.stream().filter(c -> !oneSegments.contains(c)).findFirst().get();
            map.put('a', aMapping);

            final var zeroSixNine = getDigitsWithCount(6);
            var zsnSegments = zeroSixNine.stream().flatMap(digit -> digit.segments().stream()).collect(Collectors.toList());
            zsnSegments.removeIf(sevenSegments::contains);
            zsnSegments.removeIf(fourSegments::contains);
            final var sssMap = new HashMap<Character, Integer>();
            for (final var c : zsnSegments) {
                sssMap.compute(c, (_key, old) -> old == null ? 1 : old + 1);
            }
            if(sssMap.size() != 2) {
                throw new IllegalStateException("More segments for 0, 6, 9 encountered: " + sssMap);
            }
            for (final var entry : sssMap.entrySet()) {
                if (entry.getValue() == 3) {
                    map.put('g', entry.getKey());
                } else if (entry.getValue() == 2) {
                    map.put('e', entry.getKey());
                } else {
                    throw new IllegalStateException();
                }
            }

            final var twoFiveThree = getDigitsWithCount(5);
            var tftSegments = twoFiveThree.stream().flatMap(digit -> digit.segments.stream()).collect(Collectors.toList());
            tftSegments.removeIf(sevenSegments::contains);
            tftSegments.removeIf(candidate -> candidate.equals(map.get('e')));
            tftSegments.removeIf(candidate -> candidate.equals(map.get('g')));
            final var tftCounts = new HashMap<Character, Integer>();
            for(final var c : tftSegments) {
                tftCounts.compute(c, (_key, old) -> old == null ? 1 : old + 1);
            }
            for(final var entry : tftCounts.entrySet()) {
                if(entry.getValue() == 3) {
                    map.put('d', entry.getKey());
                } else if(entry.getValue() == 1) {
                    map.put('b', entry.getKey());
                } else {
                    throw new IllegalStateException();
                }
            }

            zsnSegments = zeroSixNine.stream().flatMap(digit -> digit.segments().stream()).collect(Collectors.toList());
            zsnSegments.removeIf(candidate -> candidate.equals(map.get('a')));
            zsnSegments.removeIf(candidate -> candidate.equals(map.get('b')));
            zsnSegments.removeIf(candidate -> candidate.equals(map.get('d')));
            zsnSegments.removeIf(candidate -> candidate.equals(map.get('e')));
            zsnSegments.removeIf(candidate -> candidate.equals(map.get('g')));
            final var zsnCounts = new HashMap<Character, Integer>();
            for(final var c : zsnSegments) {
                zsnCounts.compute(c, (_key, old) -> old == null ? 1 : old + 1);
            }
            for(final var entry : zsnCounts.entrySet()) {
                if(entry.getValue() == 2) {
                    map.put('c', entry.getKey());
                } else if( entry.getValue() == 3) {
                    map.put('f', entry.getKey());
                } else {
                    throw new IllegalStateException();
                }
            }

            return map;
        }

        public static Entry parse(final String string) {
            final var components = string.split(" \\| ");
            final var uniqueSignalPatterns = components[0].split(" ");
            final var outputValue = components[1].split(" ");

            return new Entry(Arrays.stream(uniqueSignalPatterns)
                    .map(Digit::parse)
                    .collect(Collectors.toUnmodifiableList()),
                    Arrays.stream(outputValue)
                            .map(Digit::parse)
                            .collect(Collectors.toUnmodifiableList()));
        }

    }

    @Test
    public final void part1() {
        final var result = getInput()
                .map(Entry::parse)
                .flatMap(entry -> entry.outputValue().stream())
                .filter(digit -> {
                    final var segments = digit.segments();
                    final var numSegments = segments.size();
                    return numSegments == 2 || numSegments == 4 || numSegments == 3 || numSegments == 7;
                })
                .count();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var result = getInput()
                .map(Entry::parse)
                .mapToInt(Entry::decodeOutput).sum();

        System.out.println("Part 2: " + result);
    }

}
package com.macasaet;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 20: Trench Map ---
 */
public class Day20 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-20.txt"),
                        false);
    }

    public record ImageEnhancementAlgorithm(boolean[] map) {

        public boolean isPixelLit(final int code) {
            return map()[code];
        }

        public static ImageEnhancementAlgorithm parse(final String line) {
            final var map = new boolean[line.length()];
            for (int i = line.length(); --i >= 0; map[i] = line.charAt(i) == '#') ;
            return new ImageEnhancementAlgorithm(map);
        }
    }

    protected record Coordinate(int x, int y) {
    }

    public record Image(SortedMap<Integer, SortedMap<Integer, Boolean>> pixels, int minX, int maxX, int minY,
                        int maxY, boolean isEven) {
        public Image enhance(final ImageEnhancementAlgorithm algorithm) {
            final var enhancedPixelMap = new TreeMap<Integer, SortedMap<Integer, Boolean>>();
            int enhancedMinX = minX;
            int enhancedMaxX = maxX;
            int enhancedMinY = minY;
            int enhancedMaxY = maxY;
            for (int i = minX - 1; i <= maxX + 1; i++) {
                final var targetRow = new TreeMap<Integer, Boolean>();
                for (int j = minY - 1; j <= maxY + 1; j++) {
                    final int replacementId = decode(i, j, !isEven && algorithm.isPixelLit(0));
                    final var shouldLight = algorithm.isPixelLit(replacementId);
                    if (shouldLight) {
                        // save space by only storing an entry when lit
                        targetRow.put(j, true);
                        enhancedMinY = Math.min(enhancedMinY, j);
                        enhancedMaxY = Math.max(enhancedMaxY, j);
                    }
                }
                if (!targetRow.isEmpty()) {
                    // save space by only storing a row if at least one cell is lit
                    enhancedPixelMap.put(i, Collections.unmodifiableSortedMap(targetRow));
                    enhancedMinX = Math.min(enhancedMinX, i);
                    enhancedMaxX = Math.max(enhancedMaxX, i);
                }
            }
            return new Image(Collections.unmodifiableSortedMap(enhancedPixelMap), enhancedMinX, enhancedMaxX, enhancedMinY, enhancedMaxY, !isEven);
        }

        int decode(final int x, final int y, boolean voidIsLit) {
            final var list = getNeighbouringCoordinates(x, y).stream()
                    .map(coordinate -> isBitSet(coordinate, voidIsLit))
                    .toList();
            int result = 0;
            for (int i = list.size(); --i >= 0; ) {
                if (list.get(i)) {
                    final int shiftDistance = list.size() - i - 1;
                    result |= 1 << shiftDistance;
                }
            }
            if (result < 0 || result > 512) {
                throw new IllegalStateException("Unable to decode pixel at " + x + ", " + y);
            }
            return result;
        }

        boolean isBitSet(final Coordinate coordinate, boolean voidIsLit) {
            final var row = pixels().get(coordinate.x());
            if((coordinate.x() < minX || coordinate.x() > maxX) && row == null) {
                return voidIsLit;
            }
            else if(row == null) {
                return false;
            }
            return row.getOrDefault(coordinate.y(), (coordinate.y() < minY || coordinate.y() > maxY) && voidIsLit);
        }

        List<Coordinate> getNeighbouringCoordinates(int x, int y) {
            return Arrays.asList(
                    new Coordinate(x - 1, y - 1),
                    new Coordinate(x - 1, y),
                    new Coordinate(x - 1, y + 1),
                    new Coordinate(x, y - 1),
                    new Coordinate(x, y),
                    new Coordinate(x, y + 1),
                    new Coordinate(x + 1, y - 1),
                    new Coordinate(x + 1, y),
                    new Coordinate(x + 1, y + 1)
            );
        }

        public long countLitPixels() {
            return pixels().values()
                    .stream()
                    .flatMap(row -> row.values().stream())
                    .filter(isLit -> isLit)
                    .count();
        }

        public int width() {
            return maxX - minX + 1;
        }

        public int height() {
            return maxY - minY + 1;
        }

        public String toString() {
            final var builder = new StringBuilder();
            builder.append(width()).append('x').append(height()).append('\n');
            for (int i = minX; i <= maxX; i++) {
                final var row = pixels.getOrDefault(i, Collections.emptySortedMap());
                for (int j = minY; j <= maxY; j++) {
                    final var value = row.getOrDefault(j, false);
                    builder.append(value ? '#' : '.');
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        public static Image parse(final List<String> lines) {
            final var pixels = new TreeMap<Integer, SortedMap<Integer, Boolean>>();
            final int minX = 0;
            final int minY = 0;
            int maxX = 0;
            int maxY = 0;
            for (int i = lines.size(); --i >= 0; ) {
                final var line = lines.get(i);
                final var row = new TreeMap<Integer, Boolean>();
                for (int j = line.length(); --j >= 0; ) {
                    final var pixel = line.charAt(j);
                    row.put(j, pixel == '#');
                    if (pixel == '#') {
                        row.put(j, true);
                        maxY = Math.max(maxY, j);
                    }
                }
                if (!row.isEmpty()) {
                    maxX = Math.max(maxX, i);
                    pixels.put(i, Collections.unmodifiableSortedMap(row));
                }
            }
            return new Image(Collections.unmodifiableSortedMap(pixels), minX, maxX, minY, maxY, true);
        }

    }

    @Nested
    public class ImageTest {
        @Test
        public final void testToInt() {
            final var string = """
                    #..#.
                    #....
                    ##..#
                    ..#..
                    ..###
                    """;
            final var image = Image.parse(Arrays.asList(string.split("\n")));
            assertEquals(34, image.decode(2, 2, false));
        }

        @Test
        public final void flipAllOn() {
            final var template = "#........";
            final var imageString = """
                    ...
                    ...
                    ...
                    """;
            final var image = Image.parse(Arrays.asList(imageString.split("\n")));
            final var result = image.enhance(ImageEnhancementAlgorithm.parse(template));
            assertTrue(result.pixels().get(1).get(1));
        }

        @Test
        public final void turnOffPixel() {
            final var templateBuilder = new StringBuilder();
            for (int i = 511; --i >= 0; templateBuilder.append('#')) ;
            templateBuilder.append('.');
            final var template = templateBuilder.toString();
            final var imageString = """
                    ###
                    ###
                    ###
                    """;
            final var image = Image.parse(Arrays.asList(imageString.split("\n")));
            final var result = image.enhance(ImageEnhancementAlgorithm.parse(template));
            final var middleRow = result.pixels().get(1);
            assertFalse(middleRow.containsKey(1));
        }
    }

    @Test
    public final void part1() {
        final var list = getInput().toList();
        final var algorithm = ImageEnhancementAlgorithm.parse(list.get(0));
        final var image = Image.parse(list.subList(2, list.size()))
                .enhance(algorithm)
                .enhance(algorithm);
        System.out.println("Part 1: " + image.countLitPixels());
    }

    @Test
    public final void part2() {
        final var list = getInput().toList();
        final var algorithm = ImageEnhancementAlgorithm.parse(list.get(0));
        var image = Image.parse(list.subList(2, list.size()));
        for(int _i = 50; --_i >= 0; image = image.enhance(algorithm));
        System.out.println("Part 2: " + image.countLitPixels());
    }

}
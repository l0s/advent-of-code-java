package com.macasaet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Day20 {

    private static final String seaMonsterString =
            "                  # \n" +
            "#    ##    ##    ###\n" +
            " #  #  #  #  #  #   ";
    private static final char[][] seaMonster;

    static {
        final var seaMonsterLines = seaMonsterString.split("\n");
        seaMonster = new char[seaMonsterLines.length][];
        for (int i = seaMonsterLines.length; --i >= 0; seaMonster[i] = seaMonsterLines[i].toCharArray()) ;
    }

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day20.class.getResourceAsStream("/day-20-input.txt"))) {
            final var tiles = new LinkedList<Tile>();
            final var lines =
                    StreamSupport.stream(spliterator, false)
                            .map(String::strip)
                            .collect(Collectors.toUnmodifiableList());
            int tileId = -1;
            var rows = new ArrayList<char[]>();
            for (final var line : lines) {
                if (line.startsWith("Tile ")) {
                    tileId = Integer.parseInt(line.replaceAll("[^0-9]", ""));
                    rows = new ArrayList<>();
                } else if (line.isBlank()) {
                    final var tile = new Tile(tileId, rows.toArray(new char[rows.size()][]));
                    tiles.add(tile);
                    tileId = -1;
                    rows = new ArrayList<>();
                } else {
                    rows.add(line.toCharArray());
                }
            }
            if (tileId > 0) {
                final var tile = new Tile(tileId, rows.toArray(new char[rows.size()][]));
                tiles.add(tile);
            }
            final int size = (int) Math.sqrt(tiles.size()); // the image is square

            final var possibleArrangements = getValidArrangements(Collections.emptyList(), tiles, size);
            if (possibleArrangements.isEmpty()) {
                throw new IllegalStateException("No possible arrangements");
            } else {
                // there are multiple possible arrangements, but they are just rotated and/or flipped versions of each other
                // TODO is there a matrix transform I can put in hashCode/equals that will treat these as equivalent?
                System.err.println(possibleArrangements.size() + " possible arrangements: " + possibleArrangements);
                for (final var arrangement : possibleArrangements) {
                    final var topLeft = arrangement.get(0);
                    final var topRight = arrangement.get(size - 1);
                    final var bottomLeft = arrangement.get(arrangement.size() - size);
                    final var bottomRight = arrangement.get(arrangement.size() - 1);
                    final var result =
                            Stream.of(topLeft, topRight, bottomLeft, bottomRight)
                                    .map(corner -> corner.id)
                                    .map(Long::valueOf)
                                    .map(BigInteger::valueOf)
                                    .reduce(BigInteger::multiply)
                                    .get();
                    System.out.println("Part 1: " + result);

                    final var orderedCroppedTiles =
                            arrangement.stream().map(Tile::removeBorders).collect(Collectors.toUnmodifiableList());
                    final var combined = combine(orderedCroppedTiles);

                    for (final var permutation : combined.getPermutations()) {
                        final var counter = new AtomicInteger(0);
                        highlightSeaMonsters(permutation, counter::set);
                        final var numSeaMonsters = counter.get();
                        if (numSeaMonsters > 0) {
                            System.err.println(permutation + " has " + numSeaMonsters + " sea monsters");
                            int sum = 0;
                            for (int i = permutation.edgeLength; --i >= 0; ) {
                                for (int j = permutation.edgeLength; --j >= 0; ) {
                                    if (permutation.grid[i][j] == '#') {
                                        sum++;
                                    }
                                }
                            }
                            System.out.println("Part 2: " + sum);
                        }
                    }
                }
            }
        }
    }

    public static void highlightSeaMonsters(final Tile tile, final IntConsumer counter) {
        final int windowHeight = seaMonster.length;
        final int windowWidth = seaMonster[0].length;
        final int verticalWindows = tile.edgeLength - windowHeight;
        final int horizontalWindows = tile.edgeLength - windowWidth;

        int sum = 0;
        for (int i = 0; i < verticalWindows; i++) {
            for (int j = 0; j < horizontalWindows; j++) {
                if (contains(tile.grid, i, j)) {
                    sum++;
                    highlight(tile.grid, i, j);
                }
            }
        }
        counter.accept(sum);
    }

    protected static boolean contains(final char[][] image, final int verticalOffset, final int horizontalOffset) {
        for (int i = verticalOffset; i < verticalOffset + seaMonster.length; i++) { // loop the height of the pattern
            final var patternRow = seaMonster[i - verticalOffset];
            final var imageRow = image[i];
            for (int j = horizontalOffset; j < horizontalOffset + patternRow.length; j++) { // loop the width of the pattern
                final var p = patternRow[j - horizontalOffset];
                // spaces can be anything
                if (p == '#' && imageRow[j] != '#') {
                    // only the # need to match
                    return false;
                }
            }
        }
        return true;
    }

    protected static void highlight(final char[][] image, final int verticalOffset, final int horizontalOffset) {
        for (int i = verticalOffset; i < verticalOffset + seaMonster.length; i++) {
            final var patternRow = seaMonster[i - verticalOffset];
            final var imageRow = image[i];
            for (int j = horizontalOffset; j < horizontalOffset + patternRow.length; j++) {
                final var p = patternRow[j - horizontalOffset];
                if (p == ' ') continue;
                if (p == '#') imageRow[j] = 'O';
            }
        }
    }

    protected static Set<List<Tile>> getValidArrangements(final List<Tile> partialArrangement,
                                                          final List<Tile> remainingTiles,
                                                          final int edgeLength) {
        if (remainingTiles.isEmpty()) {
            return Collections.singleton(partialArrangement);
        } else if (partialArrangement.isEmpty()) {
            // find the candidates for the top-left tile
            final Set<List<Tile>> set = new HashSet<>();
            for (int i = remainingTiles.size(); --i >= 0; ) {
                final var candidate = remainingTiles.get(i); // candidate for first tile
                // consider all possible orientations
                for (final var orientation : candidate.getPermutations()) {
//                    System.err.println("Trying " + orientation + " in the top left with.");
                    final var partial = Collections.singletonList(orientation);
                    final var remaining = new ArrayList<Tile>(remainingTiles.size() - 1);
                    remaining.addAll(remainingTiles.subList(0, i));
                    remaining.addAll(remainingTiles.subList(i + 1, remainingTiles.size()));

                    final var validArrangements =
                            getValidArrangements(partial, Collections.unmodifiableList(remaining), edgeLength);
                    if (!validArrangements.isEmpty()) {
                        System.err.println("Found arrangement with " + orientation + " in the top left.");
                    }
                    set.addAll(validArrangements);
                }
            }
            return Collections.unmodifiableSet(set);
        }

        final Set<List<Tile>> set = new HashSet<>();
        for (int i = remainingTiles.size(); --i >= 0; ) {
            final var candidate = remainingTiles.get(i);
            final var oriented = fits(partialArrangement, candidate, edgeLength);
            if (oriented != null) {
//                System.err.println(oriented + " fits at index " + partialArrangement.size());
                final var permutation = new ArrayList<>(partialArrangement);
                permutation.add(oriented); // this is a new valid partial arrangement (is it the only one?)

                final var remaining = new ArrayList<Tile>(remainingTiles.size() - 1);
                remaining.addAll(remainingTiles.subList(0, i));
                remaining.addAll(remainingTiles.subList(i + 1, remainingTiles.size()));

                final var validArrangements =
                        getValidArrangements(Collections.unmodifiableList(permutation),
                                Collections.unmodifiableList(remaining), edgeLength);
                set.addAll(validArrangements);
            }
        }
        return Collections.unmodifiableSet(set);
    }

    protected static Tile getTileAbove(final List<Tile> partialArrangement, final int index, final int edgeLength) {
        if (index < edgeLength) {
            return null;
        }
        return partialArrangement.get(index - edgeLength);
    }

    protected static Tile getTileToLeft(final List<Tile> partialArrangement, final int index, final int edgeLength) {
        if (index % edgeLength == 0) {
            return null;
        }
        return partialArrangement.get(index - 1);
    }

    protected static Tile fits(final List<Tile> partialArrangement, final Tile candidate, final int edgeLength) {
        final int index = partialArrangement.size();
        final var tileAbove = getTileAbove(partialArrangement, index, edgeLength);
        final var tileToLeft = getTileToLeft(partialArrangement, index, edgeLength);
        for (final var orientation : candidate.getPermutations()) {
            final var topFits = tileAbove == null || tileAbove.getBottomBorder().equals(orientation.getTopBorder());
            final var leftFits = tileToLeft == null || tileToLeft.getRightBorder().equals(orientation.getLeftBorder());
            if (topFits && leftFits) {
                return orientation;
            }
        }
        return null;
    }

    protected static Tile combine(final List<Tile> arrangement) {
        final int tilesOnEdge = (int) Math.sqrt(arrangement.size());
        final var combinedLength = tilesOnEdge * arrangement.get(0).edgeLength;
        char[][] combinedGrid = new char[combinedLength][];
        int maxId = Integer.MIN_VALUE;
        int id = 0;
        for (int index = arrangement.size(); --index >= 0; ) {
            final var tile = arrangement.get(index);
            maxId = Math.max(maxId, tile.id);
            id = id * 31 + tile.id;
            final int tileRow = index / tilesOnEdge;
            final int tileColumn = index % tilesOnEdge;
            final int rowOffset = tile.edgeLength * tileRow;
            final int columnOffset = tile.edgeLength * tileColumn;
            for (int row = tile.edgeLength; --row >= 0; ) {
                if (combinedGrid[row + rowOffset] == null) {
                    combinedGrid[row + rowOffset] = new char[combinedLength];
                }
                for (int column = tile.edgeLength; --column >= 0; ) {
                    combinedGrid[row + rowOffset][column + columnOffset] = tile.grid[row][column];
                }
            }
        }
        return new Tile(id % maxId, combinedGrid);
    }

    protected static class Tile {
        private final int id;
        private final char[][] grid;
        private final int edgeLength;
        private final String label;

        public Tile(final int id, final char[][] grid) {
            this(id, grid, "original");
        }

        protected Tile(final int id, final char[][] grid, final String label) {
            this.id = id;
            this.grid = grid;
            this.edgeLength = grid.length;
            this.label = label;
        }

        public String getTopBorder() {
            return new String(grid[0]);
        }

        public String getBottomBorder() {
            return new String(grid[edgeLength - 1]);
        }

        public String getLeftBorder() {
            final char[] array = new char[edgeLength];
            for (int i = edgeLength; --i >= 0; array[i] = grid[i][0]) ;
            return new String(array);
        }

        public String getRightBorder() {
            final char[] array = new char[edgeLength];
            for (int i = edgeLength; --i >= 0; array[i] = grid[i][edgeLength - 1]) ;
            return new String(array);
        }

        public Collection<Tile> getPermutations() {
            return List.of(this, flipHorizontal(), flipVertical(), rotate90(), rotate180(), rotate270(),
                    rotate90().flipHorizontal(), rotate90().flipVertical(),
                    rotate180().flipHorizontal(), rotate180().flipVertical(),
                    rotate270().flipHorizontal(), rotate270().flipVertical());
        }

        public Tile flipVertical() {
            final var flipped = new char[edgeLength][];
            for (int row = edgeLength; --row >= 0; ) {
                final var flippedRow = new char[edgeLength];
                for (int oldColumn = edgeLength; --oldColumn >= 0; ) {
                    final int newColumn = edgeLength - oldColumn - 1;
                    flippedRow[newColumn] = grid[row][oldColumn];
                }
                flipped[row] = flippedRow;
            }
            return new Tile(id, flipped, label + ", flipped around vertical axis");
        }

        public Tile flipHorizontal() {
            final var flipped = new char[edgeLength][];
            for (int i = edgeLength; --i >= 0; ) {
                final int newRowId = edgeLength - i - 1;
                final char[] row = new char[edgeLength];
                System.arraycopy(grid[i], 0, row, 0, edgeLength);
                flipped[newRowId] = row;
            }
            return new Tile(id, flipped, label + ", flipped around horizontal axis");
        }

        public Tile transpose() {
            final var transposed = new char[edgeLength][]; // should this be its own permutation?
            for (int i = edgeLength; --i >= 0; ) {
                transposed[i] = new char[edgeLength];
                for (int j = edgeLength; --j >= 0; ) {
                    transposed[i][j] = grid[j][i];
                }
            }
            return new Tile(id, transposed, label + ", transposed");
        }

        public Tile rotate90() {
            final var transposed = transpose().grid;
            final var reversedRows = new char[edgeLength][];
            for (int i = edgeLength; --i >= 0; ) {
                final var newRow = new char[edgeLength];
                for (int j = edgeLength; --j >= 0; ) {
                    final int newColumn = edgeLength - j - 1;
                    newRow[newColumn] = transposed[i][j];
                }
                reversedRows[i] = newRow;
            }
            return new Tile(id, reversedRows, label + ", rotated 90 degrees");
        }

        public Tile rotate180() {
            return new Tile(id, rotate90().rotate90().grid, label + ", rotated 180 degrees");
        }

        public Tile rotate270() {
            return new Tile(id, rotate180().rotate90().grid, label + ", rotated 270 degrees");
        }

        public Tile removeBorders() {
            final int length = edgeLength - 2;
            final var cropped = new char[length][];
            for (int i = edgeLength - 1; --i >= 1; ) {
                final var row = new char[length];
                System.arraycopy(grid[i], 1, row, 0, length);
                cropped[i - 1] = row;
            }
            return new Tile(id, cropped, label + " cropped");
        }

        public String toString() {
            return "Tile{ " + id + ", " + label + ", top=" + getTopBorder() + " }";
        }

        public int hashCode() {
            int retval = 0;
            retval = 31 * retval + id;
            for (int i = edgeLength; --i >= 0; ) {
                for (int j = edgeLength; --j >= 0; ) {
                    retval = 31 * retval + grid[i][j];
                }
            }
            return retval;
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            } else if (this == o) {
                return true;
            }
            try {
                final Tile other = (Tile) o;
                boolean retval = id == other.id;
                for (int i = edgeLength; --i >= 0 && retval; ) {
                    for (int j = edgeLength; --j >= 0 && retval; ) {
                        retval &= grid[i][j] == other.grid[i][j];
                    }
                }
                return retval;
            } catch (final ClassCastException cce) {
                return false;
            }
        }
    }

}
package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day17 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day17.class.getResourceAsStream("/day-17-input.txt"))) {
            final var grid = new Grid();

            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());
            for( int x = 0; x < lines.size(); x++ ) {
                final var line = lines.get(x);
                for( int y = 0; y < line.length(); y++ ) {
                    final char state = line.charAt(y);
                    final int z = 0;
                    final int w = 0;
                    grid.setInitial(x, y, z, w, state);
                }
            }

            for( int i = 0; i < 6; i++ ) {
                grid.cycle();
            }
            System.out.println( "Part 2: " + grid.countActive() );
        }
    }

    public static class Grid {

        private final SortedMap<Integer, SortedMap<Integer, SortedMap<Integer, SortedMap<Integer, Character>>>> map = new TreeMap<>();
        private int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0, minW = 0, maxW = 0;

        public void setInitial(final int x, final int y, final int z, final int w, final char state) {
            final var dimX = map.computeIfAbsent(x, key -> new TreeMap<>());
            final var dimY = dimX.computeIfAbsent(y, key -> new TreeMap<>());
            final var dimZ = dimY.computeIfAbsent(z, key -> new TreeMap<>());
            dimZ.put(w, state);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
            minW = Math.min(minW, w);
            maxW = Math.max(maxW, w);
        }

        public Runnable defer(final int x, final int y, final int z, final int w) {
            int activeNeighbours = 0;
            for( final var neighbour : neighbours( x, y, z, w ) ) {
                if( neighbour.getState() == '#' ) {
                    activeNeighbours++;
                }
            }
            final var cell = new Cell(x, y, z, w);
            if( cell.getState() == '#' ) { // active
                return activeNeighbours == 2 || activeNeighbours == 3 ? () -> {} : () -> cell.setState('.');
            } else { // inactive
                return activeNeighbours == 3 ? () -> cell.setState('#') : () -> {};
            }
        }

        public void cycle() {
            final var updateTasks = new LinkedList<Runnable>();
            for( int x = minX - 1; x <= maxX + 1; x++ ) {
                for( int y = minY - 1; y <= maxY + 1; y++ ) {
                    for( int z = minZ - 1; z <= maxZ + 1; z++ ) {
                        for( int w = minW - 1; w <= maxW + 1; w++ ) {
                            updateTasks.add(defer(x, y, z, w));
                        }
                    }
                }
            }
            updateTasks.forEach(Runnable::run);
        }

        public int countActive() {
            return map.values()
                .stream()
                .flatMapToInt(yDim -> yDim.values()
                        .stream()
                        .flatMapToInt(zDim -> zDim.values()
                                .stream()
                                .flatMapToInt(wDim -> wDim.values()
                                        .stream()
                                        .mapToInt(state -> state == '#' ? 1 : 0 ))))
                    .sum();
        }

        protected Collection<Cell> neighbours(final int x, final int y, final int z, int w) {
            final var list = new ArrayList<Cell>(80);
            for( int i = x - 1; i <= x + 1; i++ ) {
                for( int j = y - 1; j <= y + 1; j++ ) {
                    for( int k = z - 1; k <= z + 1; k++ ) {
                        for( int l = w - 1; l <= w + 1; l++ ) {
                            if (i == x && j == y && k == z && l == w) continue;
                            list.add(new Cell(i, j, k, l));
                        }
                    }
                }
            }
            if( list.size() != 80 ) {
                throw new IllegalStateException("There should be 80 neighbours :-(");
            }
            return Collections.unmodifiableList(list);
        }

        protected class Cell {
            final int x, y, z, w;

            public Cell(final int x, final int y, final int z, int w) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.w = w;
            }

            public char getState() {
                final var dimensionX = map.getOrDefault(x, Collections.emptySortedMap());
                final var dimensionY = dimensionX.getOrDefault(y, Collections.emptySortedMap());
                final var dimensionZ = dimensionY.getOrDefault(z, Collections.emptySortedMap());
                return dimensionZ.getOrDefault(w, '.');
            }

            public void setState(final char state) {
                final var dimensionX = map.computeIfAbsent(x, key -> new TreeMap<>());
                final var dimensionY = dimensionX.computeIfAbsent(y, key -> new TreeMap<>());
                final var dimensionZ = dimensionY.computeIfAbsent(z, key -> new TreeMap<>());
                dimensionZ.put(w, state);
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
                minZ = Math.min(minZ, z);
                maxZ = Math.max(maxZ, z);
                minW = Math.min(minW, w);
                maxW = Math.max(maxW, w);
            }
        }
    }
}
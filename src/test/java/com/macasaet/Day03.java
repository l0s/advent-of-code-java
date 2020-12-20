package com.macasaet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day03 {

    public static void main(String[] args) throws IOException {
        try( var spliterator = new LineSpliterator( Day03.class.getResourceAsStream("/day-3-input.txt" ) ) ) {
            final var rowList = StreamSupport.stream(spliterator, false)
                    .map(String::toCharArray)
                    .collect(Collectors.toUnmodifiableList());
            final var matrix = new char[rowList.size()][];
            for (int i = rowList.size(); --i >= 0; matrix[i] = rowList.get(i));

            final var slopes = new int[][] {
                    new int[] { 1, 1 },
                    new int[] { 3, 1 }, // uncomment all but this for "part 1"
                    new int[] { 5, 1 },
                    new int[] { 7, 1 },
                    new int[] { 1, 2 },
            };
            final var totalTrees = Arrays.stream(slopes)
                    .mapToLong(pair -> {
                    final int slopeRight = pair[0];
                    final int slopeDown = pair[1];

                    long numTrees = 0;
                    int rowIndex = 0;
                    int columnIndex = 0;

                    do {
                        final var row = matrix[rowIndex];
                        final var cell = row[columnIndex];
                        if (cell == '#') {
                            numTrees += 1l;
                        }
                        rowIndex += slopeDown;
                        columnIndex = columnIndex + slopeRight;
                        columnIndex = columnIndex % row.length; // "These aren't the only trees, though; due to
                        // something you read about once involving
                        // arboreal genetics and biome stability, the
                        // same pattern repeats to the right many times"
                    } while( rowIndex < matrix.length );
                    return numTrees;
                }).mapToObj(BigInteger::valueOf) // I wasn't sure how large these (multiplied) values could get, in retrospect, `long` would have been fine
                .reduce(BigInteger::multiply)
                .get();
            System.out.println("" + totalTrees.toString());
        }

    }

}
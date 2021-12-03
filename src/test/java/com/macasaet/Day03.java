package com.macasaet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 3: Binary Diagnostic ---
 */
public class Day03 {

    /**
     * @return "a list of binary numbers which, when decoded properly, can tell you many useful things about the
     *          conditions of the submarine"
     */
    protected Stream<byte[]> getDiagnosticReport() {
        return StreamSupport
                .stream(new LineSpliterator("day-03.txt"),
                        false)
                .map(string -> {
                    final var chars = string.toCharArray();
                    final var bits = new byte[chars.length];
                    for (int i = chars.length; --i >= 0; bits[i] = chars[i] == '0' ? (byte) 0 : (byte) 1) ;
                    return bits;
                });
    }

    protected int toUnsignedInt(final byte[] bits) {
        int result = 0;
        for (int i = bits.length; --i >= 0; result += bits[i] * Math.pow(2, bits.length - i - 1)) ;
        return result;
    }

    @Test
    public final void part1() {
        final var list = getDiagnosticReport().collect(Collectors.toList());
        final int width = list.get(0).length;
        final int[] zeroCounts = new int[width];
        for (int i = zeroCounts.length; --i >= 0; zeroCounts[i] = 0) ;
        final int[] oneCounts = new int[width];
        for (int i = oneCounts.length; --i >= 0; oneCounts[i] = 0) ;
        for (final var array : list) {
            for (int j = 0; j < width; j++) {
                if (array[j] == 0) {
                    zeroCounts[j] += 1;
                } else {
                    oneCounts[j] += 1;
                }
            }
        }
        final byte[] gammaArray = new byte[width];
        final byte[] epsilonArray = new byte[width];
        for (int i = gammaArray.length; --i >= 0; ) {
            gammaArray[i] = zeroCounts[i] > oneCounts[i] ? (byte) 0 : (byte) 1;
            epsilonArray[i] = zeroCounts[i] > oneCounts[i] ? (byte) 1 : (byte) 0;
        }

        final int gammaRate = toUnsignedInt(gammaArray);
        final int epsilonRate = toUnsignedInt(epsilonArray);
        System.out.println("Part 1: " + (gammaRate * epsilonRate));
    }

    @Test
    public final void part2() {
        final var list = getDiagnosticReport().collect(Collectors.toList());
        final int width = list.get(0).length;
        List<byte[]> oxygenCandidates = new ArrayList<>(list);
        for (int i = 0; i < width && oxygenCandidates.size() > 1; i++) {
            int zeros = 0;
            int ones = 0;
            for (final var value : oxygenCandidates) {
                if (value[i] == 0) {
                    zeros++;
                } else {
                    ones++;
                }
            }
            final int index = i;
            if (ones >= zeros) {
                oxygenCandidates = oxygenCandidates.stream().filter(value -> value[index] == 1).collect(Collectors.toList());
            } else {
                oxygenCandidates = oxygenCandidates.stream().filter(value -> value[index] == 0).collect(Collectors.toList());
            }
        }
        if (oxygenCandidates.size() > 1) {
            throw new IllegalStateException("Too many oxygen candidates");
        }
        List<byte[]> co2Candidates = new ArrayList<>(list);
        for (int i = 0; i < width && co2Candidates.size() > 1; i++) {
            int zeros = 0;
            int ones = 0;
            for (final var value : co2Candidates) {
                if (value[i] == 0) {
                    zeros++;
                } else {
                    ones++;
                }
            }
            final int index = i;
            if (zeros <= ones) {
                co2Candidates = co2Candidates.stream().filter(value -> value[index] == 0).collect(Collectors.toList());
            } else {
                co2Candidates = co2Candidates.stream().filter(value -> value[index] == 1).collect(Collectors.toList());
            }
        }
        if (co2Candidates.size() > 1) {
            throw new IllegalStateException("Too many CO2 candidates");
        }
        final byte[] oxyArray = oxygenCandidates.get(0);
        final byte[] co2Array = co2Candidates.get(0);
        final int oxygenGeneratorRating = toUnsignedInt(oxyArray);
        final int co2ScrubberRating = toUnsignedInt(co2Array);
        System.out.println("Part 2: " + (oxygenGeneratorRating * co2ScrubberRating));
    }

}
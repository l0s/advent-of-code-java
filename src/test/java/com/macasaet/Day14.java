package com.macasaet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day14 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day14.class.getResourceAsStream("/day-14-input.txt"))) {
            final var memory = new HashMap<BigInteger, BigInteger>();
            char[] mask = null;
            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());
            for (final var line : lines) {
                final var components = line.split(" = ");
                final var stringValue = components[1].strip();
                if ("mask".equalsIgnoreCase(components[0].strip())) {
                    mask = stringValue.strip().toCharArray();
                } else {
                    final var address = new BigInteger(components[0].replaceAll("[^0-9]", ""));
                    var binaryString = Integer.toBinaryString(Integer.parseInt(stringValue));
                    final int padSize = 36 - binaryString.length();
                    final var pad = new char[padSize];
                    for (int i = padSize; --i >= 0; pad[i] = '0') ;
                    binaryString = new String(pad) + binaryString;

                    final var valueBinary = new char[36];
                    for (int j = 36; --j >= 0; ) {
                        if (mask[j] == 'X') {
                            valueBinary[j] = binaryString.charAt(j);
                        } else {
                            valueBinary[j] = mask[j];
                        }
                    }

                    final var result = toInt(valueBinary);
                    memory.put(address, result);
                }
            }
            var sum = memory.values().stream().reduce(BigInteger::add).get();
            System.out.println("Part 1: " + sum);

            memory.clear();
            mask = null;
            for (final var line : lines) {
                final var components = line.split(" = ");
                final var stringValue = components[1].strip();
                if ("mask".equalsIgnoreCase(components[0].strip())) {
                    mask = stringValue.toCharArray();
                } else {
                    final var addressDecimal = Integer.parseInt(components[0].strip().replaceAll("[^0-9]", ""));
                    var addressBinaryString = Integer.toBinaryString(addressDecimal);
                    final int padSize = 36 - addressBinaryString.length();
                    final var addressSpec = new char[36];
                    for (int i = 0; i < padSize; addressSpec[i++] = '0') ;
                    for (int i = 0; i < addressBinaryString.length(); i++) {
                        addressSpec[i + padSize] = addressBinaryString.charAt(i);
                    }
                    for (int i = 36; --i >= 0; ) {
                        if (mask[i] == '1') {
                            addressSpec[i] = '1';
                        } else if (mask[i] == 'X') {
                            addressSpec[i] = 'X';
                        }
                    }
                    final var value = toInt(Integer.parseInt(components[1].strip()));
                    for (final var address : explode(addressSpec)) {
                        memory.put(address, value);
                    }
                }
            }
            sum = memory.values().stream().reduce(BigInteger::add).get();
            System.out.println("Part 2: " + sum);
        }
    }

    protected static BigInteger toInt(final int decimal) {
        final var string = Integer.toBinaryString(decimal);
        return toInt(string.toCharArray());
    }

    protected static BigInteger toInt(final char[] chars) {
        var retval = BigInteger.ZERO;
        for (int i = chars.length; --i >= 0; ) {
            final int power = chars.length - i - 1;
            final var multiplier = chars[i] == '0' ? BigInteger.ZERO : BigInteger.ONE;
            retval = retval.add(BigInteger.TWO.pow(power).multiply(multiplier));
        }
        return retval;
    }

    protected static List<BigInteger> explode(final char[] chars) {
        final var floatingIndices = new ArrayList<Integer>();
        for (int i = 36; --i >= 0; ) {
            if (chars[i] == 'X') {
                floatingIndices.add(i);
            }
        }
        return explode(chars, floatingIndices);
    }

    protected static List<BigInteger> explode(final char[] chars, final List<Integer> floatingIndices) {
        if (floatingIndices.isEmpty()) {
            return Collections.singletonList(toInt(chars));
        }
        final var index = floatingIndices.get(0);

        var list = new ArrayList<BigInteger>();

        final var copy = Arrays.copyOf(chars, 36);
        final var sub = floatingIndices.subList(1, floatingIndices.size());

        copy[index] = '0';
        list.addAll(explode(copy, sub));
        copy[index] = '1';
        list.addAll(explode(copy, sub));

        return Collections.unmodifiableList(list);
    }

}
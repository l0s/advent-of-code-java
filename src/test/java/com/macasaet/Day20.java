package com.macasaet;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 20: Grove Positioning System ---
 * <a href="https://adventofcode.com/2022/day/20">https://adventofcode.com/2022/day/20</a>
 */
public class Day20 {

    public record Number(int originalIndex, int value, BigInteger decryptedValue) {
        Number(int originalIndex, int value) {
            this(originalIndex, value, BigInteger.valueOf(value).multiply(BigInteger.valueOf(811_589_153)));
        }
    }

    protected static List<Number> getInput() {
        final List<Integer> numbers = StreamSupport.stream(new LineSpliterator("day-20.txt"), false)
                .mapToInt(Integer::parseInt)
                .collect(ArrayList::new, (x, y) -> x.add(y), (x, y) -> x.addAll(y));
        final var result = new ArrayList<Number>(numbers.size());
        for(int i = 0; i < numbers.size(); i++) {
            result.add(new Number(i, numbers.get(i)));
        }
        return Collections.unmodifiableList(result);
    }

    @Test
    public final void part1() {
        final var numbers = getInput();
        final var indexMap = new HashMap<Integer, Integer>(numbers.size());
        Number zero = null;
        for(final var number : numbers) {
            indexMap.put(number.originalIndex, number.value());
            if(number.value() == 0) {
                zero = number;
            }
        }
        final var workingSet = new ArrayList<>(numbers);

        for(final var number : numbers) {
            final var originalIndex = workingSet.indexOf(number);
            workingSet.remove(originalIndex);
            var newIndex = (originalIndex + number.value()) % (numbers.size() - 1);
            if(newIndex < 0) {
                newIndex += numbers.size() - 1;
            }
            workingSet.add(newIndex, number);
        }

        final var x = workingSet.get((workingSet.indexOf(zero) + 1000) % workingSet.size()).value();
        final var y = workingSet.get((workingSet.indexOf(zero) + 2000) % workingSet.size()).value();
        final var z = workingSet.get((workingSet.indexOf(zero) + 3000) % workingSet.size()).value();

        final var result = (long)x + (long)y + (long)z;

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var numbers = getInput();
        final var indexMap = new HashMap<Integer, Integer>(numbers.size());
        Number zero = null;
        for(final var number : numbers) {
            indexMap.put(number.originalIndex, number.value());
            if(number.value() == 0) {
                zero = number;
            }
        }
        final var workingSet = new ArrayList<>(numbers);

        for(int i = 10; --i >= 0; ) {
            for (final var number : numbers) {
                final var originalIndex = workingSet.indexOf(number);
                workingSet.remove(originalIndex);
                var newIndex = number.decryptedValue().add(BigInteger.valueOf(originalIndex)).mod(BigInteger.valueOf(numbers.size() - 1)).intValue();
                if (newIndex < 0) {
                    newIndex += numbers.size() - 1;
                }
                workingSet.add(newIndex, number);
            }
        }

        final var x = workingSet.get((workingSet.indexOf(zero) + 1000) % workingSet.size()).decryptedValue();
        final var y = workingSet.get((workingSet.indexOf(zero) + 2000) % workingSet.size()).decryptedValue();
        final var z = workingSet.get((workingSet.indexOf(zero) + 3000) % workingSet.size()).decryptedValue();

        final var result = x.add(y).add(z);

        System.out.println("Part 2: " + result);
    }

}
package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 6: ---
 * <a href="https://adventofcode.com/2022/day/6">https://adventofcode.com/2022/day/6</a>
 */
public class Day06 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-06.txt"),
                        false);
    }

    @Test
    public final void part1() {
        final var input = getInput().findFirst().get();
        for(int i = 4; i < input.length(); i++) {
            final var set = new HashSet<Character>(4);
            for(int j = 0; j < 4; j++) {
                set.add(input.charAt(i - 4 + j));
            }
            if(set.size() >= 4) {
                final var result = i;
                System.out.println("Part 1: " + result);
                return;
            }
        }
        throw new IllegalStateException();
    }

    @Test
    public final void part2() {
        final var input = getInput().findFirst().get();
        for(int i = 14; i < input.length(); i++) {
            final var set = new HashSet<Character>(14);
            for(int j = 0; j < 14; j++) {
                set.add(input.charAt(i - 14 + j));
            }
            if(set.size() >= 14) {
                final var result = i;
                System.out.println("Part 2: " + result);
                return;
            }
        }
        throw new IllegalStateException();
    }

}
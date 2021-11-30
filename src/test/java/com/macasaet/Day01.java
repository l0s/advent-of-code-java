package com.macasaet;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;


public class Day01 {

    protected Stream<String> getInput() {
        return StreamSupport.stream(new LineSpliterator(getClass().getResourceAsStream("/day-1-input.txt")),
                false);
    }

    @Test
    public final void part1() {

    }

    @Test
    public final void part2() {

    }
}

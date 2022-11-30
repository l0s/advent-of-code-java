package com.macasaet;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * --- Day 1:  ---
 */
public class Day01 {

    /**
     *
     *
     * @return
     */
    protected List<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-01.txt"),
                        false)
                .collect(ArrayList::new, List::add, List::addAll);
    }

    @Disabled
    @Test
    public final void part1() {
        final var list = getInput();

        System.out.println("Part 1: " + null);
    }

    @Disabled
    @Test
    public final void part2() {
        final var list = getInput();

        System.out.println("Part 2: " + null);
    }

}
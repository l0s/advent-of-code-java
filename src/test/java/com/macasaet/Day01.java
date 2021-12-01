package com.macasaet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 1: Sonar Sweep ---
 */
public class Day01 {

    /**
     * Perform a sonar sweep of the nearby sea floor.
     *
     * @return measurements of the sea floor depth further and further away from the submarine
     */
    protected List<Integer> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-01.txt"),
                        false)
                .mapToInt(Integer::parseInt)
                .collect(ArrayList::new, List::add, List::addAll);
    }

    @Test
    public final void part1() {
        final var list = getInput();
        int increases = countIncreases(list);
        System.out.println("Part 1: " + increases);
    }

    @Test
    public final void part2() {
        final var list = getInput();
        final var windows = new LinkedList<Integer>();
        for (int i = 2; i < list.size(); i++) {
            windows.add(list.get(i) + list.get(i - 1) + list.get(i - 2));
        }
        final int increases = countIncreases(windows);
        System.out.println("Part 2: " + increases);
    }

    /**
     * Determine how quickly the depth increases.
     *
     * @param list progressively further measurements of the sea floor depth
     * @return the number of times a depth measurement increase from the previous measurement
     */
    protected int countIncreases(final List<? extends Integer> list) {
        int previous = list.get(0);
        int increases = 0;
        for (int i = 1; i < list.size(); i++) {
            final var current = list.get(i);
            if (current > previous) {
                increases++;
            }
            previous = current;
        }
        return increases;
    }

}
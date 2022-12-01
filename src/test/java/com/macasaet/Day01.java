package com.macasaet;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * --- Day 1: Calorie Counting ---
 */
public class Day01 {

    protected Iterator<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-01.txt"),
                        false)
                .iterator();
    }

    protected List<Elf> getElves() {
        var calories = new ArrayList<BigInteger>();
        final var elves = new ArrayList<Elf>();
        for (final var i = getInput(); i.hasNext(); ) {
            final var line = i.next();
            if (line.isBlank()) {
                elves.add(new Elf(Collections.unmodifiableList(calories)));
                calories = new ArrayList<>();
            } else {
                calories.add(new BigInteger(line.strip()));
            }
        }
        if (!calories.isEmpty()) {
            elves.add(new Elf(Collections.unmodifiableList(calories)));
        }
        return Collections.unmodifiableList(elves);
    }

    @Test
    public final void part1() {
        final var elves = getElves();
        final var elf = elves.stream()
                .max(Comparator.comparing(Elf::totalCaloriesCarried))
                .get();

        System.out.println("Part 1: " + elf.totalCaloriesCarried());
    }

    @Test
    public final void part2() {
        final var elves = getElves();
        final var list = elves.stream()
                .sorted(Comparator.comparing(Elf::totalCaloriesCarried).reversed())
                .toList();

        System.out.println("Part 2: " + (list.get(0).totalCaloriesCarried().add(list.get(1).totalCaloriesCarried()).add(list.get(2).totalCaloriesCarried())));
    }

    /**
     * An elf who collects food for the reindeer.
     *
     * @param itemCalories The number of calories of each item carried by the elf
     */
    public record Elf(List<BigInteger> itemCalories) {
        public BigInteger totalCaloriesCarried() {
            return itemCalories().stream()
                    .reduce(BigInteger::add)
                    .get();
        }
    }

}
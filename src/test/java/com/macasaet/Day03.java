package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 3: Rucksack Reörganisation ---
 * https://adventofcode.com/2022/day/3
 */
public class Day03 {

    protected static int priority(final char c) {
        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 1;
        }
        return c - 'A' + 27;
    }

    protected Stream<Rucksack> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-03.txt"),
                        false)
                .map(Rucksack::parse);
    }

    @Test
    public final void part1() {
        final var result = getInput().mapToInt(Rucksack::priority).sum();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var groups = new ArrayList<List<Rucksack>>();
        var currentGroup = new ArrayList<Rucksack>(3);

        for (final var i = getInput().iterator(); i.hasNext(); ) {
            final var rucksack = i.next();
            if (currentGroup.size() == 3) {
                groups.add(Collections.unmodifiableList(currentGroup));
                currentGroup = new ArrayList<>(3);
            }
            currentGroup.add(rucksack);
        }
        if (currentGroup.size() == 3) {
            groups.add(Collections.unmodifiableList(currentGroup));
        }
        final var result = groups.stream().map(this::getBadge).mapToInt(Day03::priority).sum();

        System.out.println("Part 2: " + result);
    }

    protected char getBadge(final List<? extends Rucksack> group) {
        final var first = group.get(0);
        for (final var item : first.allItems()) {
            if (group.get(1).allItems().contains(item) && group.get(2).allItems().contains(item)) {
                return item;
            }
        }
        throw new IllegalStateException();
    }

    /**
     * An Elf's container of supplies for a jungle journey. "Each rucksack has two large compartments. All items of a
     * given type are meant to go into exactly one of the two compartments."
     *
     * @param firstCompartment  All the items in one compartment
     * @param secondCompartment All the items in one compartment
     * @param allItems          All the items
     */
    public record Rucksack(Set<Character> firstCompartment, Set<Character> secondCompartment, Set<Character> allItems) {

        public static Rucksack parse(final String line) {
            final var chars = line.toCharArray();
            if (chars.length % 2 != 0) {
                throw new IllegalArgumentException();
            }
            final var firstCompartment = new HashSet<Character>(chars.length / 2);
            final var secondCompartment = new HashSet<Character>(chars.length / 2);
            for (int i = 0; i < chars.length / 2; i++) {
                firstCompartment.add(chars[i]);
            }
            for (int i = chars.length / 2; i < chars.length; i++) {
                secondCompartment.add(chars[i]);
            }
            final var union = new HashSet<Character>(chars.length);
            union.addAll(firstCompartment);
            union.addAll(secondCompartment);
            return new Rucksack(Collections.unmodifiableSet(firstCompartment),
                    Collections.unmodifiableSet(secondCompartment),
                    Collections.unmodifiableSet(union));
        }

        public int priority() {
            final var intersection = new HashSet<Character>();
            for (final char c : firstCompartment) {
                if (secondCompartment.contains(c)) {
                    intersection.add(c);
                }
            }
            if (intersection.size() != 1) {
                throw new IllegalStateException("There should only be one common item between compartments");
            }
            return Day03.priority(intersection.iterator().next());
        }


    }
}
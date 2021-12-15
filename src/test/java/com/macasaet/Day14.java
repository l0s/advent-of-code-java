package com.macasaet;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 14: Extended Polymerization ---
 */
public class Day14 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-14.txt"),
                        false);
    }

    public record Polymer(Map<ElementPair, BigInteger> pairCounts, char firstElement, char lastElement) {
        public static Polymer forTemplate(final String templateString) {
            final var firstElement = templateString.charAt(0);
            final var lastElement = templateString.charAt(templateString.length() - 1);
            final var map = new HashMap<ElementPair, BigInteger>();
            for (int i = 1; i < templateString.length(); i++) {
                map.merge(new ElementPair(templateString.charAt(i - 1), templateString.charAt(i)),
                        BigInteger.ONE,
                        BigInteger::add);
            }
            return new Polymer(Collections.unmodifiableMap(map), firstElement, lastElement);
        }

        /**
         * Apply the pair insertion process one time.
         *
         * @param rules pair insertion rules for generating a new polymer
         * @return the new polymer that results
         */
        public Polymer applyRules(final Map<ElementPair, PairInsertionRule> rules) {
            final var map = new HashMap<ElementPair, BigInteger>();
            for (final var entry : pairCounts().entrySet()) {
                final var key = entry.getKey();
                final var count = entry.getValue();
                final var rule = rules.get(key);
                final var left = new ElementPair(key.start(), rule.insert());
                final var right = new ElementPair(rule.insert(), key.end());

                map.compute(left, (_key, oldCount) -> oldCount == null ? count : oldCount.add(count));
                map.compute(right, (_key, oldCount) -> oldCount == null ? count : oldCount.add(count));
            }
            return new Polymer(Collections.unmodifiableMap(map), firstElement(), lastElement());
        }

        /**
         * Determine how many times each element appears in the polymer
         *
         * @return the number of times each element appears in the polymer
         */
        public SortedMap<BigInteger, Set<Character>> histogram() {
            final var map = new HashMap<Character, BigInteger>();
            for (final var entry : pairCounts().entrySet()) {
                final var pair = entry.getKey();
                final var count = entry.getValue();
                map.compute(pair.start(),
                        (_key, oldValue) -> oldValue == null ? count : oldValue.add(count));
                map.compute(pair.end(),
                        (_key, oldValue) -> oldValue == null ? count : oldValue.add(count));
            }
            for (final var entry : map.entrySet()) {
                final var element = entry.getKey();
                final var count = entry.getValue();
                if (element.equals(firstElement()) || element.equals(lastElement())) {
                    entry.setValue(count.divide(BigInteger.TWO).add(BigInteger.ONE));
                } else {
                    entry.setValue(count.divide(BigInteger.TWO));
                }
            }
            final var result = new TreeMap<BigInteger, Set<Character>>();
            for (final var entry : map.entrySet()) {
                final var target = result.computeIfAbsent(entry.getValue(), _key -> new HashSet<>());
                target.add(entry.getKey());
            }
            return Collections.unmodifiableSortedMap(result);
        }
    }

    /**
     * A pair of elements that appear adjacent to each other. This may be used in the context of a pair insertion rule
     * definition or a polymer.
     *
     * @see Polymer
     * @see PairInsertionRule
     */
    protected record ElementPair(char start, char end) {
    }

    /**
     * A single instruction to aid in finding the optimal polymer formula
     */
    public record PairInsertionRule(char start, char end, char insert) {

        public static PairInsertionRule parse(final String string) {
            final var components = string.split(" -> ");
            final var match = components[0].toCharArray();
            return new PairInsertionRule(match[0], match[1], components[1].toCharArray()[0]);
        }

    }

    protected record Input(Polymer polymerTemplate, List<PairInsertionRule> rules) {
    }

    protected Input parseInput() {
        final var list = getInput().collect(Collectors.toList());
        int mode = 0;
        Polymer polymer = null;
        final var rules = new ArrayList<PairInsertionRule>();
        for (final var line : list) {
            if (line.isBlank()) {
                mode++;
                continue;
            }
            if (mode == 0) {
                polymer = Polymer.forTemplate(line);
            } else {
                rules.add(PairInsertionRule.parse(line));
            }
        }
        return new Input(polymer, rules);
    }

    @Test
    public final void part1() {
        final var input = parseInput();
        var polymer = input.polymerTemplate();
        final var rules = input.rules();
        final var ruleMap = new HashMap<ElementPair, PairInsertionRule>();
        for (final var rule : rules) {
            ruleMap.put(new ElementPair(rule.start(), rule.end()), rule);
        }
        for (int _i = 0; _i < 10; _i++) {
            polymer = polymer.applyRules(ruleMap);
        }
        final var histogram = polymer.histogram();
        System.out.println("Part 1: " + histogram.lastKey().subtract(histogram.firstKey()));
    }

    @Test
    public final void part2() {
        final var input = parseInput();
        var polymer = input.polymerTemplate();
        final var rules = input.rules();
        final var ruleMap = new HashMap<ElementPair, PairInsertionRule>();
        for (final var rule : rules) {
            ruleMap.put(new ElementPair(rule.start(), rule.end()), rule);
        }
        for (int _i = 0; _i < 40; _i++) {
            polymer = polymer.applyRules(ruleMap);
        }
        final var histogram = polymer.histogram();
        System.out.println("Part 2: " + histogram.lastKey().subtract(histogram.firstKey()));
    }

}
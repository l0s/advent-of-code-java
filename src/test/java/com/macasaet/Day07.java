package com.macasaet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class Day07 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day07.class.getResourceAsStream("/day-7-input.txt"))) {
            final var bag = "shiny gold";
            final var ruleMap = StreamSupport.stream(spliterator, false)
                    .map(Rule::fromSentence)
                    .collect(HashMap<String, Rule>::new,
                            (map, rule) -> map.put(rule.containerColour, rule),
                            Map::putAll);
            final var count = ruleMap.values()
                    .stream()
                    .filter(rule -> rule.canContain(bag, ruleMap))
                    .count();
            System.out.println("part 1: " + count);
            System.out.println("part 2: " + (ruleMap.get(bag).countContained(ruleMap) - 1));
        }
    }

    protected static class Rule {
        final String containerColour;
        final Map<String, Integer> containedCounts;

        public Rule(final String containerColour, final Map<String, Integer> containedCounts) {
            // TODO validation
            this.containerColour = containerColour;
            this.containedCounts = containedCounts;
        }

        public int countContained(final Map<String, Rule> ruleMap) {
            return 1 + containedCounts.entrySet().stream().mapToInt(entry -> {
                final var containedColour = entry.getKey();
                final int multiplier = entry.getValue();

                final var subRule = ruleMap.get(containedColour);
                final int base = subRule.countContained(ruleMap);
                return base * multiplier;
            }).sum();
        }

        public boolean canContain(final String colour, final Map<String, Rule> ruleMap) {
            if (containedCounts.containsKey(colour)) {
                return true;
            }
            return containedCounts.keySet()
                    .stream()
                    .map(ruleMap::get)
                    .anyMatch(rule -> rule != null && rule.canContain(colour, ruleMap));
        }

        public static Rule fromSentence(final String sentence) {
            final var components = sentence.split(" bags contain ", 2);
            if ("no other bags.".equalsIgnoreCase(components[1])) {
                return new Rule(components[0], Collections.emptyMap());
            }
            final var containedPhrases = components[1].split(", ");
            final var containedCounts = Arrays.stream(containedPhrases)
                    .map(phrase -> phrase.replaceFirst(" bag.*$", ""))
                    .map(phrase -> phrase.split(" ", 2))
                    .collect(HashMap<String, Integer>::new,
                            (map, phraseComponents) -> map.put(phraseComponents[1], Integer.parseInt(phraseComponents[0])),
                            Map::putAll);
            return new Rule(components[0], Collections.unmodifiableMap(containedCounts));
        }
    }
}
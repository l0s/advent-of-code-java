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
            final var ruleMap = new HashMap<String, Rule>();
            StreamSupport.stream(spliterator, false)
                    .map(Rule::fromSentence)
                    .forEach(rule -> ruleMap.put(rule.containerColour, rule));
            int count = 0;
            for (final var rule : ruleMap.values()) {
                if (rule.canContain(bag, ruleMap)) {
                    count++;
                }
            }
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
            int retval = 1;
            for(final var entry : containedCounts.entrySet()) {
                final var containedColour = entry.getKey();
                final int multiplier = entry.getValue();

                final var subRule = ruleMap.get(containedColour);
                final int base = subRule.countContained(ruleMap);
                retval = retval + (base * multiplier);
            }
            return retval;
        }

        public boolean canContain(final String colour, final Map<String, Rule> ruleMap) {
            if (containedCounts.containsKey(colour)) {
                return true;
            }
            for (final var containedColour : containedCounts.keySet()) {
                final var rule = ruleMap.get(containedColour);
                if (rule != null && rule.canContain(colour, ruleMap)) {
                    return true;
                }
            }
            return false;
        }

        public static Rule fromSentence(final String sentence) {
            final var components = sentence.split(" bags contain ");
            final var containedCounts = new HashMap<String, Integer>();
            if (!"no other bags.".equalsIgnoreCase(components[1])) {
                final var containedPhrases = components[1].split(", ");
                Arrays.stream(containedPhrases)
                        .map(phrase -> phrase.replaceFirst(" bag.*$", ""))
                        .forEach(phrase -> {
                    final var phraseComponents = phrase.split(" ", 2);
                    containedCounts.put(phraseComponents[1], Integer.parseInt(phraseComponents[0]));
                });
            }
            return new Rule(components[0], Collections.unmodifiableMap(containedCounts));
        }
    }
}
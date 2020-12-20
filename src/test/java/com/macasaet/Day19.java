package com.macasaet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Day19 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day19.class.getResourceAsStream("/day-19-input.txt"))) {
            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());

            final var ruleMap = new HashMap<Integer, Rule>();
            int sum = 0;
            int mode = 0;
            for (var line : lines) {
                // comment out the remapping for Part 1
                if ("8: 42".equals(line.strip())) {
                    line = "8: 42 | 42 8";
                } else if ("11: 42 31".equals(line.strip())) {
                    line = "11: 42 31 | 42 11 31";
                }
                if (line.isBlank()) {
                    mode++;
                    continue;
                }
                if (mode == 0) {
                    final var components = line.split(": ", 2);
                    final var ruleId = Integer.parseInt(components[0].strip());
                    final var value = components[1].strip();
                    if (value.matches("\"[a-z]\"")) {
                        final var c = value.charAt(1);
                        final var rule = new CharacterRule(ruleId, c);
                        ruleMap.put(ruleId, rule);
                    } else if (value.contains("|")) {
                        final var ruleSets = value.split(" \\| ");
                        final var set = Arrays.stream(ruleSets)
                                .map(ruleSet -> Arrays.stream(ruleSet.split(" "))
                                        .map(String::strip)
                                        .map(Integer::parseInt)
                                        .collect(Collectors.toUnmodifiableList()))
                                .collect(Collectors.toUnmodifiableSet());
                        final var rule = new DisjunctionRule(ruleId, set);
                        ruleMap.put(ruleId, rule);
                    } else {
                        final var subRules = Arrays.stream(value.split(" "))
                                .map(String::strip)
                                .map(Integer::parseInt)
                                .collect(Collectors.toUnmodifiableList());
                        final var rule = new SequenceRule(ruleId, subRules);
                        ruleMap.put(ruleId, rule);
                    }
                } else {
                    final var rule = ruleMap.get(0);
                    if (rule.matches(line.strip(), ruleMap)) {
                        sum++;
                    }
                }
            }
            System.out.println("Result: " + sum);
        }
    }

    public abstract static class Rule {
        protected final int id;

        protected Rule(final int id) {
            this.id = id;
        }

        public boolean matches(final String string, final Map<? super Integer, ? extends Rule> map) {
            return getMatchingSuffixes(Stream.of(string), map).distinct().anyMatch(String::isBlank);
        }

        protected abstract Stream<String> getMatchingSuffixes(Stream<String> strings, Map<? super Integer, ? extends Rule> map);
    }

    public static class CharacterRule extends Rule {

        private final char c;

        public CharacterRule(final int id, final char c) {
            super(id);
            this.c = c;
        }

        protected Stream<String> getMatchingSuffixes(Stream<String> strings, Map<? super Integer, ? extends Rule> map) {
            return strings.flatMap(string ->
                    string.startsWith("" + c)
                            ? Stream.of(string.substring(1))
                            : Stream.empty());
        }
    }

    public static class SequenceRule extends Rule {
        private final List<? extends Integer> ruleIds;

        public SequenceRule(final int id, final List<? extends Integer> ruleIds) {
            super(id);
            this.ruleIds = ruleIds;
        }

        protected Stream<String> getMatchingSuffixes(Stream<String> strings, Map<? super Integer, ? extends Rule> map) {
            return strings.flatMap(string -> {
                var result = Stream.of(string);
                for (final var ruleId : ruleIds) { // match all in sequence
                    final var rule = map.get(ruleId);
                    final var iterator = rule.getMatchingSuffixes(result, map).iterator();
                    if (!iterator.hasNext()) {
                        result = Stream.empty();
                        break;
                    }
                    result = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
                }
                return result;
            }).distinct();
        }
    }

    public static class DisjunctionRule extends Rule {

        private final Collection<? extends List<? extends Integer>> options;

        public DisjunctionRule(final int id, final Collection<? extends List<? extends Integer>> options) {
            super(id);
            this.options = options;
        }

        protected Stream<String> getMatchingSuffixes(final Stream<String> strings, final Map<? super Integer, ? extends Rule> map) {
            return strings.flatMap(string -> options.stream().flatMap(option -> {
                var result = Stream.of(string);
                for (final var ruleId : option) { // match all in sequence
                    final var rule = map.get(ruleId);
                    final var iterator = rule.getMatchingSuffixes(result, map).iterator();
                    if (!iterator.hasNext()) {
                        result = Stream.empty();
                        break;
                    }
                    result = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
                }
                return result;
            })).distinct();
        }
    }
}
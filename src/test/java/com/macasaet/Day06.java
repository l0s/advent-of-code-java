package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day06 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day06.class.getResourceAsStream("/day-6-input.txt"))) {
            final var rawLines = StreamSupport.stream(spliterator, false)
                    .collect(Collectors.toUnmodifiableList());
            var currentGroup = new ArrayList<String>();
            // collect all the text blocks into entries (separated by empty lines)
            final var groups = new ArrayList<List<String>>();
            for (final var line : rawLines) {
                if (line.isBlank()) {
                    if (currentGroup != null && !currentGroup.isEmpty()) {
                        groups.add(currentGroup);
                        currentGroup = new ArrayList<>();
                    }
                } else {
                    currentGroup.add(line);
                }
            }
            if (currentGroup != null && !currentGroup.isEmpty()) {
                groups.add(currentGroup);
            }
            final int sum = groups.stream().mapToInt(group -> {
                final var uniqueCharacters = group.stream()
                        .flatMapToInt(String::chars)
                        .collect(HashSet<Integer>::new, Set::add, Set::addAll);
                return (int) uniqueCharacters.stream()
                        .filter(c -> (int) group.stream()
                            .map(String::chars)
                            .filter(chars -> chars.anyMatch(answer -> answer == c))
                            .count() == group.size())
                        .count();
            }).sum();
            System.out.println(sum);
        }
    }

}
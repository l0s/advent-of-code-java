package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day04 {

    public static void main(String[] args) throws IOException {
        try (var spliterator = new LineSpliterator( Day04.class.getResourceAsStream( "/day-4-input.txt" ) ) ) {
            final var rawLines = StreamSupport.stream(spliterator, false)
                    .collect(Collectors.toUnmodifiableList());
            String current = "";
            // collect all the text blocks into entries (separated by empty lines)
            final var entries = new ArrayList<String>();
            for (final var line : rawLines) {
                if (line.isBlank()) {
                    if (!current.isBlank()) {
                        entries.add(current);
                        current = "";
                    }
                } else {
                    current += " " + line;
                }
            }
            if (!current.isBlank()) {
                entries.add(current);
            }
            int numValid = 0;
            for (final var entry : entries) {
                final var pairs = entry.split("\\s");
                final var map = new HashMap<String, String>();
                for (final var pair : pairs) {
                    if (pair.isBlank()) {
                        continue;
                    }
                    final var components = pair.split(":", 2);
                    map.put(components[0].trim(), components[1].trim());
                }
                final var birthYearString = map.get("byr");
                final var issueYearString = map.get("iyr");
                final var expirationYearString = map.get("eyr");
                final var heightString = map.get("hgt");
                final var hairColour = map.get("hcl");
                final var eyeColour = map.get("ecl");
                final var validEyeColours = Set.of("amb", "blu", "brn", "gry", "grn", "hzl", "oth");
                final var passportId = map.get("pid");
                if (birthYearString == null) continue;
                final int birthYear = Integer.parseInt(birthYearString);
                if (birthYear < 1920 || birthYear > 2002) continue;
                if (issueYearString == null) continue;
                final int issueYear = Integer.parseInt(issueYearString);
                if (issueYear < 2010 || issueYear > 2020) continue;
                if (expirationYearString == null) continue;
                final int expirationYear = Integer.parseInt(expirationYearString);
                if (expirationYear < 2020 || expirationYear > 2030) continue;
                if (heightString == null) continue;
                if (heightString.endsWith("cm")) {
                    final int centimetres = Integer.parseInt(heightString.replace("cm", ""));
                    if (centimetres < 150 || centimetres > 193) continue;
                } else if (heightString.endsWith("in")) {
                    final int inches = Integer.parseInt(heightString.replace("in", ""));
                    if (inches < 59 || inches > 76) continue;
                } else {
                    continue;
                }
                if (hairColour == null) continue;
                if (!hairColour.matches("#[0-9a-f]{6}")) continue;
                if (eyeColour == null) continue;
                if (!validEyeColours.contains(eyeColour)) continue;
                if (passportId == null) continue;
                if (!passportId.matches("[0-9]{9}")) continue;
                numValid++;
            }
            System.out.println(numValid);
        }

    }

}
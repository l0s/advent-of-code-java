package com.macasaet;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Day15 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day15.class.getResourceAsStream("/day-15-test.txt"))) {
//            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());
//            final var line = lines.get(0);
//            final var numbers = Arrays.stream(line.split(",")).map(String::strip).map(Integer::parseInt).collect(Collectors.toUnmodifiableList());
//            final var numbers = new int[]{ 0, 3, 6 };
            final var numbers = new int[]{ 13,16,0,12,15,1 };

            int last = -1;
            final var oralHistory = new HashMap<Integer, List<Integer>>();
            for( int i = 0; i < numbers.length; i++ ) {
                final int number = numbers[ i ];
                oralHistory.computeIfAbsent(number, k -> new LinkedList<>()).add( i );
                last = number;
                System.err.println( "Speak: " + number );
            }

            for( int i = numbers.length; i < 30_000_000; i++ ) {
                final var history = oralHistory.computeIfAbsent(last, k -> new LinkedList<>());
                if( history.isEmpty() ) {
                    throw new IllegalStateException("No history for: " + last );
                } else if( history.size() == 1 ) { // spoken only once before
                    final int numberToSpeak = 0;
                    System.err.println( "Turn " + ( i + 1 ) + ": Speak: " + numberToSpeak );
                    oralHistory.getOrDefault( numberToSpeak, new LinkedList<>() ).add( i );
                    last = numberToSpeak;
                } else { // spoken 2+ times
                    final int lastMention = history.get(history.size() - 1);
                    final int penultimateMention = history.get(history.size() - 2);
                    final int numberToSpeak = lastMention - penultimateMention;
                    System.err.println( "Turn " + ( i + 1 ) + ": Speak: " + numberToSpeak );
                    oralHistory.computeIfAbsent(numberToSpeak, k -> new LinkedList<>()).add( i );
                    last = numberToSpeak;
                }
            }
            System.out.println( "Part 1: " + last );
        }
    }

}
package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class Day05 {

    public static void main(String[] args) throws IOException {
        try (var spliterator = new LineSpliterator( Day05.class.getResourceAsStream( "/day-5-input.txt" ) ) ) {
            final var seatIds = StreamSupport.stream(spliterator, false)
                .mapToInt(id -> {
                    int maxRowExclusive = 128;
                    int row = 0;
                    for( int i = 0; i < 7; i++ ) {
                        final char partition = id.charAt(i);
                        if( partition == 'B' ) {
                            row = ( ( maxRowExclusive - row ) / 2 ) + row;
                        } else if( partition == 'F' ) {
                            maxRowExclusive = maxRowExclusive - ( ( maxRowExclusive - row ) / 2 );
                        } else {
                            throw new IllegalArgumentException("Invalid row partition: " + partition);
                        }
                    }
                    int column = 0;
                    int maxColumnExclusive = 8;
                    for( int i = 7; i < 10; i++ ) {
                        final char half = id.charAt(i);
                        if( half == 'R' ) {
                            column = ( ( maxColumnExclusive - column ) / 2 ) + column;
                        } else if( half == 'L' ) {
                            maxColumnExclusive = maxColumnExclusive - ( ( maxColumnExclusive - column ) / 2 );
                        } else {
                            throw new IllegalArgumentException("Invalid column partition: " + half);
                        }
                    }
                    final int seatId = row * 8 + column;
                    return seatId;
                })
                .sorted()
                .collect(ArrayList<Integer>::new, List::add, List::addAll);
            System.out.println("part 1: " + seatIds.get(seatIds.size() - 1));
            for( int i = seatIds.size(); --i >= 1; ) {
                final int x = seatIds.get( i - 1 );
                final int y = seatIds.get( i );
                if( y - x > 1 ) {
                    System.out.println("part 2: " + ( x + 1 ) );
                    return;
                }
            }
        }

    }

}
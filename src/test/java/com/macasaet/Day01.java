package com.macasaet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.StreamSupport;


public class Day01 {

    public static void main(String[] args) throws IOException {
        try( var spliterator = new LineSpliterator(Day01.class.getResourceAsStream("/day-1-input.txt")) ) {
//            part1(spliterator);
            part2(spliterator);
        }
    }

    protected static void part1(final Spliterator<String> spliterator) {
        final var items = StreamSupport.stream(spliterator, false)
            .mapToInt(Integer::parseInt)
            .filter(candidate -> candidate <= 2020) // filter out the obvious
            .sorted() // sort to ensure the complement is to the left
            .collect(ArrayList<Integer>::new, List::add, List::addAll);
        for( int i = items.size(); --i >= 0; ) {
            final int x = items.get(i);
            for( int j = i; --j >= 0; ) { // avoid retrying combinations
                final int y = items.get(j);
                if( x + y == 2020 ) {
                    System.out.println( "" + ( x * y ) );
                    return;
                }
            }
        }
    }

    protected static void part2(final Spliterator<String> spliterator) {
        final var items = StreamSupport.stream(spliterator, false)
                .mapToInt(Integer::parseInt)
                .filter(candidate -> candidate <= 2020) // filter out the obvious
                .sorted() // sort to ensure the complements are to the left
                .collect(ArrayList<Integer>::new, List::add, List::addAll);
        for( int i = items.size(); --i >= 0; ) {
            final int x = items.get(i);
            for( int j = i; --j >= 0; ) {
                final int y = items.get(j);
                for( int k = j; --k >= 0; ) {
                    final int z = items.get(k);
                    if( x + y + z == 2020 ) {
                        System.out.println( "" + ( x * y * z ) );
                        return;
                    }
                }

            }
        }
    }
}

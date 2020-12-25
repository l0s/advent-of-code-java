package com.macasaet;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day25 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day22.class.getResourceAsStream("/day-25-input.txt"))) {
            final var publicKeys = StreamSupport.stream(spliterator, false)
                    .map(Integer::parseInt)
                    .collect(Collectors.toUnmodifiableList());
            final int cardPublicKey = publicKeys.get(0);
            final int doorPublicKey = publicKeys.get(1);
            final long initialSubjectNumber = 7;

            int cardLoopSize = 0;
            for (long value = 1; value != cardPublicKey; cardLoopSize++) {
                value = transformOnce(value, initialSubjectNumber);
            }
            System.out.println("cardLoopSize: " + cardLoopSize);

            int doorLoopSize = 0;
            for (long value = 1; value != doorPublicKey; doorLoopSize++) {
                value = transformOnce(value, initialSubjectNumber);
            }
            System.out.println("doorLoopSize: " + doorLoopSize);

            final long e1 = transformCompletely(doorPublicKey, cardLoopSize);
            final long e2 = transformCompletely(cardPublicKey, doorLoopSize);
            System.out.println("e1: " + e1);
            System.out.println("e2: " + e2);
        }
    }

    protected static long transformCompletely(final long subjectNumber, final int loopSize) {
        long value = 1;
        for (int i = loopSize; --i >= 0; value = transformOnce(value, subjectNumber)) ;
        return value;
    }

    protected static long transformOnce(final long value, final long subjectNumber) {
        return (value * subjectNumber) % 20201227;
    }

}
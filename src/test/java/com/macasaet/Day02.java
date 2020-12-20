package com.macasaet;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class Day02 {

    public static void main(String[] args) throws IOException {
        try( var spliterator = new LineSpliterator( Day02.class.getResourceAsStream("/day-2-input.txt" ) ) ) {
            final long count = StreamSupport.stream(spliterator, false)
//                .map(SledEntry::build) // part 1
                .map(TobogganEntry::build) // part 2
                .filter(Entry::isValid).count();
            System.out.println("" + count);
        }

    }

    public static abstract class Entry {

        // TODO consider a more robust pattern:
        // https://stackoverflow.com/a/4731164/914887
        protected static final Pattern separator = Pattern.compile("\\s");
        protected static final Pattern rangeSeparator = Pattern.compile("-");
        protected final char c; // TODO support code points
        protected final String password;

        protected Entry(final char c, final String password) {
            this.c = c;
            this.password = password;
        }

        public abstract boolean isValid();

    }

    public static class SledEntry extends Entry {
        private final long minIterations;
        private final long maxIterations;

        public SledEntry( final char c, final String password, final long minIterations, final long maxIterations ) {
            super(c, password);
            this.minIterations = minIterations;
            this.maxIterations = maxIterations;
        }

        public static Entry build(final String line) {
            final var components = separator.split(line, 3);
            final var range = components[0];
            final var rangeComponents = rangeSeparator.split(range, 2);
            return new SledEntry(components[1].charAt(0),
                    components[2],
                    Long.parseLong(rangeComponents[ 0 ]),
                    Long.parseLong(rangeComponents[ 1 ] ));
        }

        public boolean isValid() {
            final var count = password.chars()
                .filter(candidate -> (char) candidate == this.c)
                .count();
            return count >= minIterations && count <= maxIterations;
        }
    }

    public static class TobogganEntry extends Entry {
        private final int firstPosition;
        private final int secondPosition;

        public TobogganEntry( final char c, final String password, final int firstPosition, final int secondPosition ) {
            super( c, password );
            this.firstPosition = firstPosition;
            this.secondPosition = secondPosition;
        }

        public static Entry build( final String line ) {
            final var components = separator.split( line, 3 );
            final var positionComponents = rangeSeparator.split( components[ 0 ], 2 );
            return new TobogganEntry( components[ 1 ].charAt( 0 ),
                    components[ 2 ],
                    Integer.parseInt( positionComponents[ 0 ] ),
                    Integer.parseInt( positionComponents[ 1 ] ) );
        }

        public boolean isValid() {
            final var x = password.charAt(firstPosition - 1);
            final var y = password.charAt(secondPosition - 1);
            return x == c ^ y == c;
        }
    }
}
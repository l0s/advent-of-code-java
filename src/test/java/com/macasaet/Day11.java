package com.macasaet;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day11 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day11.class.getResourceAsStream("/day-11-input.txt"))) {
            final var list = StreamSupport.stream(spliterator, false).map(String::toCharArray).collect(Collectors.toList());
            final var original = list.toArray(new char[list.size()][]);
            var current = copy(original);
            while (true) {
                final var transformed = transform(current);
                if (areEqual(current, transformed)) {
                    break;
                }
                current = transformed;
            }
            System.out.println( "Part 1: " + countOccupiedSeats(current));
            current = copy(original);
            while( true ) {
                final var transformed = transform2(current);
                if (areEqual(current, transformed)) {
                    break;
                }
                current = transformed;
            }
            System.out.println( "Part 2: " + countOccupiedSeats(current));
        }
    }

    protected static char[][] copy(final char[][] original) {
        final char[][] retval = new char[ original.length ][];
        for( int i = original.length; --i >= 0; ) {
            final var row = original[ i ];
            final var copiedRow = new char[ row.length ];
            System.arraycopy(row, 0, copiedRow, 0, row.length);
            retval[ i ] = copiedRow;
        }
        return retval;
    }

    protected static char[][] transform2(final char[][] source) {
        final char[][] retval = new char[source.length][];
        for (int i = retval.length; --i >= 0; retval[i] = new char[source[0].length]) ;
        rows: for (int i = source.length; --i >= 0; ) {
            final var row = source[i];
            columns: for (int j = row.length; --j >= 0; ) {
                final var original = source[ i ][ j ];
                retval[ i ][ j ] = original;
                if( original == '.' ) {
                    continue;
                } else if( original == 'L' ) {
                    // North
                    for( int x = i; --x >= 0; ) {
                        final var visibleSeat = source[ x ][ j ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // North-West
                    for( int x = i, y = j; --x >= 0 && -- y >= 0; ) {
                        final var visibleSeat = source[ x ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // West
                    for( int y = j; --y >= 0; ) {
                        final var visibleSeat = source[ i ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // South-West
                    for( int x = i + 1, y = j - 1; x < source.length && y >= 0; x++, y-- ) {
                        final var visibleSeat = source[ x ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // South
                    for( int x = i + 1; x < source.length; x++ ) {
                        final var visibleSeat = source[ x ][ j ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // South-East
                    for( int x = i + 1, y = j + 1; x < source.length && y < row.length; x++, y++ ) {
                        final var visibleSeat = source[ x ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // East
                    for( int y = j + 1; y < row.length; y++ ) {
                        final var visibleSeat = source[ i ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    // North-East
                    for( int x = i - 1, y = j + 1; x >= 0 && y < row.length; x--, y++ ) {
                        final var visibleSeat = source[ x ][ y ];
                        if( visibleSeat == 'L' ) break;
                        if( visibleSeat == '#' ) continue columns;
                    }
                    retval[ i ][ j ] = '#';
                } else if( original == '#' ) {
                    int visibleNeighbours = 0;
                    // North
                    for( int x = i; --x >= 0 && visibleNeighbours < 5; ) {
                        final var visibleSeat = source[x][j];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // North-West
                    for( int x = i, y = j; --x >= 0 && -- y >= 0 && visibleNeighbours < 5; ) {
                        final var visibleSeat = source[ x ][ y ];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // West
                    for( int y = j; --y >= 0 && visibleNeighbours < 5; ) {
                        final var visibleSeat = source[i][y];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // South-West
                    for( int x = i + 1, y = j - 1; x < source.length && y >= 0 && visibleNeighbours < 5; x++, y-- ) {
                        final var visibleSeat = source[x][y];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // South
                    for( int x = i + 1; x < source.length && visibleNeighbours < 5; x++ ) {
                        final var visibleSeat = source[x][j];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // South-East
                    for( int x = i + 1, y = j + 1; x < source.length && y < row.length && visibleNeighbours < 5; x++, y++ ) {
                        final var visibleSeat = source[x][y];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // East
                    for( int y = j + 1; y < row.length && visibleNeighbours < 5; y++ ) {
                        final var visibleSeat = source[i][y];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    // North-East
                    for( int x = i - 1, y = j + 1; x >= 0 && y < row.length && visibleNeighbours < 5; x--, y++ ) {
                        final var visibleSeat = source[x][y];
                        if( visibleSeat == '#' ) {
                            visibleNeighbours++;
                            break;
                        } else if( visibleSeat == 'L' ) {
                            break;
                        }
                    }
                    if( visibleNeighbours >= 5 ) {
                        retval[ i ][ j ] = 'L';
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported char: " + original);
                }

            }
        }
        return retval;
    }

    protected static int countOccupiedSeats(final char[][] matrix) {
        int retval = 0;
        for( int i = matrix.length; --i >= 0; ) {
            final var row = matrix[ i ];
            for( int j = row.length; --j >= 0; ) {
                if( matrix[ i ][ j ] == '#' ) retval++;
            }
        }
        return retval;
    }

    protected static void print(final char[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            final var row = matrix[i];
            System.err.println(new String(row));
        }
    }

    protected static char[][] transform(final char[][] source) {
        final char[][] retval = new char[source.length][];
        for (int i = retval.length; --i >= 0; retval[i] = new char[source[0].length]) ;
        for (int i = source.length; --i >= 0; ) {
            final var row = source[i];
            for (int j = row.length; --j >= 0; ) {
                process(i, j, source, retval);
            }
        }
        return retval;
    }

    protected static boolean areEqual(final char[][] x, final char[][] y) {
        for (int i = x.length; --i >= 0; ) {
            final var row = x[i];
            for (int j = row.length; --j >= 0; ) {
                if (x[i][j] != y[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    protected static void process(final int x, final int y, final char[][] source, final char[][] target) {
        final var original = source[x][y];
        if (original == '.') {
            target[x][y] = '.';
            return;
        } else if (original == 'L') {
            target[x][y] = !isOccupied(x - 1, y - 1, source) && !isOccupied(x - 1, y, source)
                    && !isOccupied(x - 1, y + 1, source) && !isOccupied(x, y - 1, source) && !isOccupied(x, y + 1, source)
                    && !isOccupied(x + 1, y - 1, source) && !isOccupied(x + 1, y, source) && !isOccupied(x + 1, y + 1, source) ? '#' : original;
        } else if (original == '#') {
            int occupiedNeighbors = 0;
            occupiedNeighbors += isOccupied(x - 1, y - 1, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x - 1, y, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x - 1, y + 1, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x, y - 1, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x, y + 1, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x + 1, y - 1, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x + 1, y, source) ? 1 : 0;
            occupiedNeighbors += isOccupied(x + 1, y + 1, source) ? 1 : 0;
            target[x][y] = occupiedNeighbors >= 4 ? 'L' : original;
        } else {
            throw new IllegalArgumentException("Unsupported char: " + original);
        }
    }

    protected static boolean isOccupied(final int x, final int y, final char[][] matrix) {
        if (x < 0 || y < 0 || x >= matrix.length) {
            return false;
        }
        final char[] row = matrix[x];
        if (y >= row.length) {
            return false;
        }
        return row[y] == '#';
    }

}
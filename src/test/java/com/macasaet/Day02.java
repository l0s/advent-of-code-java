package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 2: Rock Paper Scissors ---
 * https://adventofcode.com/2022/day/2
 */
public class Day02 {

    protected Stream<Round> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-02.txt"),
                        false)
                .map(line -> {
                    final var components = line.strip().split(" ");
                    return new Round(Shape.forChar(components[0].charAt(0)),
                            Shape.forChar(components[1].charAt(0)),
                            ResponseStrategy.forChar(components[1].charAt(0)));
                });
    }

    @Test
    public final void part1() {
        final var result = getInput().mapToInt(Round::naiveScore).sum();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var result = getInput().mapToInt(Round::score).sum();

        System.out.println("Part 2: " + result);
    }

    /**
     * A shape that a contestant can play in a round
     */
    public enum Shape {
        Rock {
            public int score() {
                return 1;
            }

            public Shape beatenBy() {
                return Paper;
            }

            public Shape beats() {
                return Scissors;
            }
        },
        Paper {
            public int score() {
                return 2;
            }

            public Shape beatenBy() {
                return Scissors;
            }

            public Shape beats() {
                return Rock;
            }
        },
        Scissors {
            public int score() {
                return 3;
            }

            @Override
            public Shape beatenBy() {
                return Rock;
            }

            public Shape beats() {
                return Paper;
            }
        };

        public static Shape forChar(final int c) {
            switch (c) {
                case 'X':
                case 'A':
                    return Shape.Rock;
                case 'Y':
                case 'B':
                    return Shape.Paper;
                case 'Z':
                case 'C':
                    return Shape.Scissors;
            }
            throw new IllegalArgumentException();
        }

        /**
         * @return the inherent value of this shape
         */
        public abstract int score();

        /**
         * @return the shape that beats this one
         */
        public abstract Shape beatenBy();

        /**
         * @return the shape this one beats
         */
        public abstract Shape beats();
    }

    /**
     * An approach to responding to the shape played by the opponent
     */
    public enum ResponseStrategy {
        Lose {
            public Shape respond(Shape opponent) {
                return opponent.beats();
            }
        },
        Draw {
            public Shape respond(Shape opponent) {
                return opponent;
            }
        },
        Win {
            public Shape respond(Shape opponent) {
                return opponent.beatenBy();
            }
        };

        public static ResponseStrategy forChar(final char c) {
            switch (c) {
                case 'X':
                    return Lose;
                case 'Y':
                    return Draw;
                case 'Z':
                    return Win;
            }
            throw new IllegalArgumentException();
        }

        public abstract Shape respond(final Shape opponent);
    }

    /**
     * A single round of the game
     *
     * @param opponent         The shape played by the opponent
     * @param player           The shape chosen based on the original (incorrect) interpretation of the responseStrategy guide
     * @param responseStrategy The responseStrategy for responding to the opponent according to the responseStrategy guide
     */
    public record Round(Shape opponent, Shape player, ResponseStrategy responseStrategy) {
        /**
         * @return the score based on the simple (incorrect) interpretation of the strategy guide
         */
        public int naiveScore() {
            final var outcome = opponent() == player() ? 3 : opponent().beatenBy() == player() ? 6 : 0;
            return outcome + player().score();
        }

        /**
         * @return the score based on following the strategy guide
         */
        public int score() {
            final var response = responseStrategy().respond(opponent());
            final var outcome = opponent() == response ? 3 : opponent().beatenBy() == response ? 6 : 0;
            return outcome + response.score();
        }
    }

}
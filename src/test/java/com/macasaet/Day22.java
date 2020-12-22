package com.macasaet;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day22 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day22.class.getResourceAsStream("/day-22-input.txt"))) {
            final var lines = StreamSupport.stream(spliterator, false)
                    .collect(Collectors.toUnmodifiableList());

            final var player1cards = new LinkedList<Integer>();
            final var player2cards = new LinkedList<Integer>();
            var target = player1cards;
            for (final var line : lines) {
                if (line.isBlank()) {
                    target = player2cards;
                }
                if (line.matches("^[0-9]+$")) {
                    target.add(Integer.parseInt(line));
                }
            }
            var player1 = new Player(1, new LinkedList<>(player1cards));
            var player2 = new Player(2, new LinkedList<>(player2cards));

            // Part 1
            while (player1.hasCards() && player2.hasCards()) {
                final int p1 = player1.drawTop();
                final int p2 = player2.drawTop();
                if (p1 > p2) {
                    player1.addToBottom(p1);
                    player1.addToBottom(p2);
                } else {
                    player2.addToBottom(p2);
                    player2.addToBottom(p1);
                }
            }
            var winner = player1cards.isEmpty() ? player2 : player1;
            System.out.println("Part 1: " + winner.score());

            // Part 2
            player1 = new Player(1, new LinkedList<>(player1cards));
            player2 = new Player(2, new LinkedList<>(player2cards));
            winner = playGame(player1, player2);
            System.out.println("Part 2: " + winner.score());
        }
    }

    protected static Player playGame(final Player player1, final Player player2) {
        final var rounds = new HashSet<>();
        while (player1.hasCards() && player2.hasCards()) {
            final var round = new Round(player1, player2);
            if (rounds.contains(round)) {
                return player1;
            }
            rounds.add(round);
            final int p1 = player1.drawTop();
            final int p2 = player2.drawTop();
            if (player1.cardCountIsAtLeast(p1) && player2.cardCountIsAtLeast(p2)) {
                final var winner = playGame(player1.clone(p1), player2.clone(p2));
                if (winner.id == player1.id) {
                    player1.addToBottom(p1);
                    player1.addToBottom(p2);
                } else {
                    player2.addToBottom(p2);
                    player2.addToBottom(p1);
                }
            } else {
                if (p1 > p2) {
                    player1.addToBottom(p1);
                    player1.addToBottom(p2);
                } else {
                    player2.addToBottom(p2);
                    player2.addToBottom(p1);
                }
            }
        }
        return player1.hasCards() ? player1 : player2;
    }

    protected static class Player {
        final int id;
        final List<Integer> deck;

        public Player(final int id, final List<Integer> deck) {
            this.id = id;
            this.deck = deck;
        }

        public boolean hasCards() {
            return !deck.isEmpty();
        }

        public boolean cardCountIsAtLeast(final int count) {
            return deck.size() >= count;
        }

        public int drawTop() {
            return deck.remove(0);
        }

        public void addToBottom(final int card) {
            deck.add(card);
        }

        public Player clone(final int cardCount) {
            return new Player(id, new LinkedList<>(deck.subList(0, cardCount)));
        }

        public int score() {
            int retval = 0;
            int multiplier = deck.size();
            for (final var card : deck) {
                retval += card * (multiplier--);
            }
            return retval;
        }
    }

    protected static class Round {
        private final List<Integer> x;
        private final List<Integer> y;

        public Round(final List<Integer> x, final List<Integer> y) {
            this.x = List.copyOf(x);
            this.y = List.copyOf(y);
        }

        public Round(final Player x, final Player y) {
            this(x.deck, y.deck);
        }

        public int hashCode() {
            return Objects.hash(x, y);
        }

        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            } else if (o == null) {
                return false;
            }
            try {
                final Round other = (Round) o;
                return Objects.equals(x, other.x) && Objects.equals(y, other.y);
            } catch (final ClassCastException cce) {
                return false;
            }
        }
    }
}
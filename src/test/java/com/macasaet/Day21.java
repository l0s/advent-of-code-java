package com.macasaet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 21: Dirac Dice ---
 */
public class Day21 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-21.txt"),
                        false);
    }

    public static class DeterministicDie {
        int value;
        int totalRolls = 0;

        protected DeterministicDie(final int startingValue) {
            this.value = startingValue;
        }

        public DeterministicDie() {
            this(1);
        }

        public int roll() {
            final int result = value;
            value += 1;
            if (value > 100) {
                value -= 100;
            }
            totalRolls++;
            return result;
        }
    }

    public record Pawn(int position, int score) {
        public Pawn fork() {
            return new Pawn(position(), score());
        }

        public Pawn move(final int distance) {
            int newPosition = (position() + distance) % 10;
            if (newPosition == 0) {
                newPosition = 10;
            }
            final int newScore = score() + newPosition;
            return new Pawn(newPosition, newScore);
        }

        public Pawn takeTurn(final DeterministicDie die) {
            final int distance = die.roll() + die.roll() + die.roll();
            return move(distance);
        }
    }

    public record Game(Pawn player1, Pawn player2, boolean playerOnesTurn) {

    }

    public record ScoreCard(BigInteger playerOneWins, BigInteger playerTwoWins) {
        public ScoreCard add(final ScoreCard other) {
            return new ScoreCard(playerOneWins().add(other.playerOneWins()), playerTwoWins().add(other.playerTwoWins()));
        }
    }

    public static class QuantumDie {
        private final Map<Game, ScoreCard> cache = new HashMap<>();

        public ScoreCard play(final Game game) {
            if (cache.containsKey(game)) {
                return cache.get(game);
            }
            final var reverseScenario = new Game(game.player2(), game.player1(), !game.playerOnesTurn());
            if (cache.containsKey(reverseScenario)) {
                final var reverseResult = cache.get(reverseScenario);
                return new ScoreCard(reverseResult.playerTwoWins(), reverseResult.playerOneWins());
            }

            if (game.player1().score() >= 21) {
                final var result = new ScoreCard(BigInteger.ONE, BigInteger.ZERO);
                cache.put(game, result);
                return result;
            } else if (game.player2().score() >= 21) {
                final var result = new ScoreCard(BigInteger.ZERO, BigInteger.ONE);
                cache.put(game, result);
                return result;
            }

            var result = new ScoreCard(BigInteger.ZERO, BigInteger.ZERO);
            for (int i = 1; i <= 3; i++) {
                for (int j = 1; j <= 3; j++) {
                    for (int k = 1; k <= 3; k++) {
                        final int movementDistance = i + j + k;
                        final var forkResult = game.playerOnesTurn()
                                ? play(new Game(game.player1().move(movementDistance), game.player2(), false))
                                : play(new Game(game.player1(), game.player2().fork().move(movementDistance), true));
                        result = result.add(forkResult);
                    }
                }
            }
            cache.put(game, result);
            return result;
        }
    }

    @Test
    public final void part1() {
        final var lines = getInput().toList();
        final int playerOnePosition =
                Integer.parseInt(lines.get(0).replaceAll("Player . starting position: ", ""));
        final int playerTwoPosition =
                Integer.parseInt(lines.get(1).replaceAll("Player . starting position: ", ""));
        var playerOne = new Pawn(playerOnePosition, 0);
        var playerTwo = new Pawn(playerTwoPosition, 0);
        final var die = new DeterministicDie();
        while (true) {
            // player 1
            playerOne = playerOne.takeTurn(die);
            if (playerOne.score() >= 1000) {
                break;
            }

            // player 2
            playerTwo = playerTwo.takeTurn(die);
            if (playerTwo.score() >= 1000) {
                break;
            }
        }
        int losingScore = Math.min(playerOne.score(), playerTwo.score());

        System.out.println("Part 1: " + (losingScore * die.totalRolls));
    }

    @Test
    public final void part2() {
        final var lines = getInput().toList();
        final int playerOnePosition =
                Integer.parseInt(lines.get(0).replaceAll("Player . starting position: ", ""));
        final int playerTwoPosition =
                Integer.parseInt(lines.get(1).replaceAll("Player . starting position: ", ""));
        final var playerOne = new Pawn(playerOnePosition, 0);
        final var playerTwo = new Pawn(playerTwoPosition, 0);
        final var die = new QuantumDie();
        final var game = new Game(playerOne, playerTwo, true);
        final var result = die.play(game);
        final var winningScore = result.playerOneWins().max(result.playerTwoWins());
        System.out.println("Part 2: " + winningScore);
    }

}
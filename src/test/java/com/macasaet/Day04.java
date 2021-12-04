package com.macasaet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 4: Giant Squid ---
 */
public class Day04 {

    /**
     * The number of rows and columns on each square Bingo card
     */
    static final int EDGE_LENGTH = 5;

    public static class Board {
        private final int[][] grid;
        private final boolean[][] marked;

        protected Board(final int[][] grid, final boolean[][] marked) {
            this.grid = grid;
            this.marked = marked;
        }

        public Board(final int[][] grid) {
            this(grid, new boolean[grid.length][]);
            for (int i = grid.length; --i >= 0; this.marked[i] = new boolean[grid.length]) ;
        }

        public boolean isWinner() {
            // check rows
            for (int i = marked.length; --i >= 0; ) {
                final var row = marked[i];
                boolean complete = true;
                for (int j = row.length; --j >= 0 && complete; complete = row[j]) ;
                if (complete) {
                    return true;
                }
            }
            // check columns
            for (int j = marked.length; --j >= 0; ) {
                boolean complete = true;
                for (int i = marked.length; --i >= 0 && complete; complete = marked[i][j]) ;
                if (complete) {
                    return true;
                }
            }
            return false;
        }

        public int score(final int lastDrawn) {
            int sum = 0;
            for (int i = grid.length; --i >= 0; ) {
                final var row = grid[i];
                for (int j = row.length; --j >= 0; ) {
                    if (!marked[i][j]) {
                        sum += row[j];
                    }
                }
            }
            return sum * lastDrawn;
        }

        public void mark(final int drawn) {
            for (int i = grid.length; --i >= 0; ) {
                final var row = grid[i];
                for (int j = row.length; --j >= 0; ) {
                    if (row[j] == drawn) {
                        marked[i][j] = true;
                    }
                }
            }
        }
    }

    public record Game(List<Board> boards, List<Integer> numbers) {

        public int countBoards() {
            return boards().size();
        }

        public void removeBoard(final int index) {
            this.boards().remove(index);
        }

    }

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-04.txt"),
                        false);
    }

    protected Game getGame() {
        final var input = getInput().iterator();
        final var moves = input.next();

        List<Board> boards = new ArrayList<>();
        int[][] grid = null;
        int gridIndex = -1;
        while (input.hasNext()) {
            final var line = input.next();
            if (line.isBlank()) {
                if (grid != null) {
                    boards.add(new Board(grid));
                }
                grid = new int[EDGE_LENGTH][];
                for (int i = EDGE_LENGTH; --i >= 0; grid[i] = new int[EDGE_LENGTH]) ;
                gridIndex = 0;
                continue;
            }
            final var cells = line.split("\\s");
            if (cells.length > 0) {
                final var values = Arrays.stream(cells)
                        .filter(candidate -> !candidate.isBlank())
                        .mapToInt(Integer::parseInt)
                        .toArray();
                if (values.length > 0) {
                    grid[gridIndex++] = values;
                }
            }
        }
        if (grid != null) {
            boards.add(new Board(grid));
        }
        final var moveArray = Arrays.stream(moves.split(","))
                .mapToInt(Integer::parseInt)
                .collect(ArrayList<Integer>::new, List::add, List::addAll);
        return new Game(boards, moveArray);
    }

    @Test
    public final void part1() {
        final var game = getGame();
        for (final var number : game.numbers()) {
            for (final var board : game.boards()) {
                board.mark(number);
                if (board.isWinner()) {
                    final int score = board.score(number);
                    System.out.println("Part 1: " + score);
                    return;
                }
            }
        }
        throw new IllegalStateException("No winners");
    }

    @Test
    public final void part2() {
        final var game = getGame();
        for (final var number : game.numbers()) {
            if (game.countBoards() == 1) {
                final var lastWinner = game.boards().get(0);
                lastWinner.mark(number);
                if (!lastWinner.isWinner()) {
                    continue;
                }
                System.out.println("Part 2: " + lastWinner.score(number));
                return;
            }
            final List<Integer> idsToRemove = new ArrayList<>();
            for (int i = game.boards().size(); --i >= 0; ) {
                final var board = game.boards().get(i);
                board.mark(number);
                if (board.isWinner()) {
                    idsToRemove.add(i);
                }
            }
            for (final var id : idsToRemove) {
                game.removeBoard(id);
            }
        }
        throw new IllegalStateException("Tie for last place");
    }

}
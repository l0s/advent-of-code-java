package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.LinkedList;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 5: Supply Stacks ---
 * <a href="https://adventofcode.com/2022/day/5">https://adventofcode.com/2022/day/5</a>
 */
public class Day05 {

    public record CrateMover9000Instruction(int count, int from, int to) {
        public static CrateMover9000Instruction parse(final String line) {
            final var components = line.split(" ");
            final var count = Integer.parseInt(components[1]);
            final var from = Integer.parseInt(components[3]) - 1;
            final var to = Integer.parseInt(components[5]) - 1;
            return new CrateMover9000Instruction(count, from, to);
        }
        public void execute(final Deque<Character>[] columns) {
            final Deque<Character> from = columns[from()];
            final Deque<Character> to = columns[to()];
            for(int i = count(); --i >= 0; ) {
                to.push(from.pop());
            }
        }
    }

    public record CrateMover9001Instruction(int count, int from, int to) {
        public static CrateMover9001Instruction parse(final String line) {
            final var components = line.split(" ");
            final var count = Integer.parseInt(components[1]);
            final var from = Integer.parseInt(components[3]) - 1;
            final var to = Integer.parseInt(components[5]) - 1;
            return new CrateMover9001Instruction(count, from, to);
        }
        public void execute(final Deque<Character>[] columns) {
            final Deque<Character> from = columns[from()];
            final Deque<Character> to = columns[to()];
            final var buffer = new LinkedList<Character>();
            for(int i = count(); --i >= 0; ) {
                buffer.push(from.pop());
            }
            while(!buffer.isEmpty()) {
                to.push(buffer.pop());
            }
        }
    }
    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-05.txt"),
                        false);
    }

    @Test
    public final void part1() {
        int mode = 0;
        final Deque<Character>[] columns = new Deque[9];
        for(int i = columns.length; --i >= 0; columns[i] = new LinkedList<>());
        for(final var line : getInput().toList()) {
            if(line.isBlank()) {
                mode = 1;
            }
            if( mode == 0 ) {
                final var chars = line.toCharArray();
                int index = -1;
                for(int i = 0; i < chars.length; i++) {
                    if(chars[i] == '[') {
                        index = i / 4;
                    } else if(index >= 0) {
                        columns[index].addLast(chars[i]);
                        index = -1;
                    }
                }
            } else {
                if(line.isBlank()) {
                    continue;
                }
                final var instruction = CrateMover9000Instruction.parse(line);
                instruction.execute(columns);
            }
        }
        final var builder = new StringBuilder();
        for(final var column : columns) {
            if(!column.isEmpty()) {
                builder.append(column.getFirst());
            }
        }
        final var result = builder.toString();

        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        int mode = 0;
        final Deque<Character>[] columns = new Deque[9];
        for(int i = columns.length; --i >= 0; columns[i] = new LinkedList<>());
        for(final var line : getInput().toList()) {
            if(line.isBlank()) {
                mode = 1;
            }
            if( mode == 0 ) {
                final var chars = line.toCharArray();
                int index = -1;
                for(int i = 0; i < chars.length; i++) {
                    if(chars[i] == '[') {
                        index = i / 4;
                    } else if(index >= 0) {
//                        System.err.println("column[ " + index + " ].addLast( " + chars[ i ] + " )" );
                        columns[index].addLast(chars[i]);
                        index = -1;
                    }
                }
            } else {
                if(line.isBlank()) {
                    continue;
                }
                final var instruction = CrateMover9001Instruction.parse(line);
                instruction.execute(columns);
            }
        }
        final var builder = new StringBuilder();
        for(final var column : columns) {
            if(!column.isEmpty()) {
                builder.append(column.getFirst());
            }
        }
        final var result = builder.toString();

        System.out.println("Part 2: " + result);

    }

}
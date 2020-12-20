package com.macasaet;

import java.io.IOException;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day08 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day08.class.getResourceAsStream("/day-8-input.txt"))) {
            final var instructions =
                    StreamSupport.stream(spliterator, false)
                            .map(Instruction::fromLine)
                            .collect(Collectors.toUnmodifiableList());

            outer:
            for( int i = instructions.size(); --i >= 0; ) {
                final var toReplace = instructions.get(i);

                if( "acc".equalsIgnoreCase( toReplace.operation ) ) {
                    continue;
                }
                final var opposite = "nop".equalsIgnoreCase(toReplace.operation) ? "jmp" : "nop";
                final var replacement = new Instruction(opposite, toReplace.argument);

                int total = 0;
                int index = 0;

                final var visited = new HashSet<Integer>();
                while( index < instructions.size() ) {
                    final var instruction = index == i ? replacement : instructions.get(index);
                    if( visited.contains( index ) ) {
                        continue outer;
                    }
                    visited.add(index);
                    switch( instruction.operation ) {
                        case "acc":
                            total += instruction.argument;
                        case "nop":
                            index++;
                            break;
                        case "jmp":
                            index += instruction.argument;
                            break;
                        default:
                            throw new IllegalStateException("Invalid op: " + instruction.operation);
                    }
                }
                System.out.println("part 2: " + total);
                return;
            }
        }
    }

    public static class Instruction
    {
        private final String operation;
        private final int argument;

        public Instruction(final String operation, final int argument) {
            this.operation = operation;
            this.argument = argument;
        }

        public static Instruction fromLine(final String line) {
            final var components = line.split(" ");
            return new Instruction(components[ 0 ], Integer.parseInt( components[ 1 ] ) );
        }
    }
}
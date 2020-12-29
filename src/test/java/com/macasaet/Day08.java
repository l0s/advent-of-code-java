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

            instructions:
            // "exactly one instruction is corrupted" - try them all
            for (int i = instructions.size(); --i >= 0; ) {
                final var toReplace = instructions.get(i);

                if (toReplace.operation == Operation.acc) {
                    // "No acc instructions were harmed in the corruption of this boot code."
                    continue;
                }
                final var opposite = toReplace.operation == Operation.nop ? Operation.jmp : Operation.nop;
                final var replacement = new Instruction(opposite, toReplace.argument);

                int total = 0;
                int index = 0;

                final var visited = new HashSet<Integer>();
                // "The program is supposed to terminate by attempting to execute an instruction immediately after the last instruction in the file."
                while (index < instructions.size()) {
                    final var instruction = index == i ? replacement : instructions.get(index);
                    if (visited.contains(index)) {
                        // replacing the instruction causes an infinite loop
                        // try replacing a different one
                        // NB: Simply re-visiting this instruction is an infinite loop because the argument to each instruction is constant and never dependent on the value of "total"
                        continue instructions;
                    }
                    visited.add(index);
                    total = instruction.updateTotal(total);
                    index = instruction.updateIndex(index);
                }
                System.out.println("part 2: " + total);
                return; // "exactly one instruction is corrupted"
            }
        }
    }

    public enum Operation {
        acc {
            public int updateTotal(final int previousTotal, final int argument) {
                return previousTotal + argument;
            }
        },
        nop,
        jmp {
            public int updateIndex(final int previousIndex, final int argument) {
                return previousIndex + argument;
            }
        };

        public int updateIndex(final int previousIndex, final int argument) {
            return previousIndex + 1;
        }

        public int updateTotal(final int previousTotal, final int argument) {
            return previousTotal;
        }
    }

    public static class Instruction {
        private final Operation operation;
        private final int argument;

        public Instruction(final Operation operation, final int argument) {
            this.operation = operation;
            this.argument = argument;
        }

        public static Instruction fromLine(final String line) {
            final var components = line.split(" ", 2);
            return new Instruction(Operation.valueOf(components[0]), Integer.parseInt(components[1]));
        }

        public int updateIndex(final int previousIndex) {
            return operation.updateIndex(previousIndex, argument);
        }

        public int updateTotal(final int previousTotal) {
            return operation.updateTotal(previousTotal, argument);
        }
    }
}
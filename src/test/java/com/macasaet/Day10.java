package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 10: Cathode-Ray Tube ---
 * <a href="https://adventofcode.com/2022/day/10">https://adventofcode.com/2022/day/10</a>
 */
public class Day10 {

    public enum Instruction {
        noop {
            public int cycles() {
                return 0;
            }
        },
        addx {
            public int cycles() {
                return 2;
            }
        };

        public abstract int cycles();

        public static Instruction parse(final String string) {
            return Instruction.valueOf(string);
        }
    }

    public record Operation(Instruction instruction, Integer argument) {
        public List<CycleSnapshot> execute(int cycle, int register) {
            return switch (instruction()) {
                case noop -> Collections.singletonList(new CycleSnapshot(cycle + 1, register));
                case addx ->
                        List.of(new CycleSnapshot(cycle + 1, register),
                                new CycleSnapshot(cycle + 2, register + argument));
            };
        }

        public static Operation parse(final String line) {
            final var components = line.split(" ");
            final var instruction = Instruction.parse(components[0]);
            Integer argument = null;
            if(instruction == Instruction.addx) {
                argument = Integer.parseInt(components[1]);
            }
            return new Operation(instruction, argument);
        }
    }

    public record CycleSnapshot(int cycle, int register) {
        public int signalStrength() {
            return cycle() * register();
        }
    }

    public static class State {
        private int register = 1;
        private int cycle = 1;

        public List<Integer> getActivePixels() {
            return Arrays.asList(register - 1, register, register + 1);
        }

        public List<CycleSnapshot> execute(final Operation operation) {
            final var result = operation.execute(cycle, register);
            final var last = result.get(result.size() - 1);
            cycle = last.cycle();
            register = last.register();
            return result;
        }
    }

    public static class Display {
        final char[][] pixels = new char[6][];

        {
            for(int i = pixels.length; --i >= 0; pixels[i] = new char[40]);
        }

        public void update(final CycleSnapshot snapshot) {
            final var pixelIndex = snapshot.cycle() - 1;
            final var spritePositions =
                    Arrays.asList(snapshot.register() - 1, snapshot.register(), snapshot.register() + 1);
            final int row = pixelIndex / 40;
            final int column = pixelIndex % 40;
            if(row >= pixels.length) {
                return;
            }
            if(spritePositions.contains(column)) {
                pixels[row][column] = '#';
            } else {
                pixels[row][column] = '.';
            }
        }
        public String toString() {
            final var buffer = new StringBuilder();
            buffer.append(pixels[0], 0, 40);
            buffer.append('\n');
            buffer.append(pixels[1], 0, 40);
            buffer.append('\n');
            buffer.append(pixels[2], 0, 40);
            buffer.append('\n');
            buffer.append(pixels[3], 0, 40);
            buffer.append('\n');
            buffer.append(pixels[4], 0, 40);
            buffer.append('\n');
            buffer.append(pixels[5], 0, 40);
            return buffer.toString();
        }
    }

    protected Stream<Operation> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-10.txt"),
                        false)
                .map(Operation::parse);
    }

    @Test
    public final void part1() {
        final var interestingCycles = Arrays.asList(20, 60, 100, 140, 180, 220);
        final var state = new State();
        final var accumulator = new AtomicInteger(0);
        getInput().forEach(instruction -> {
            final var sideEffects = state.execute(instruction);
            for(final var sideEffect : sideEffects) {
                if(interestingCycles.contains(sideEffect.cycle)) {
//                    System.err.println("During cycle " + sideEffect.cycle() + ", register X has the value " + sideEffect.register() + ", so the signal strength is " + sideEffect.signalStrength());
                    accumulator.addAndGet(sideEffect.signalStrength());
                }
            }
        });
        final var result = accumulator.get();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var state = new State();
        final var display = new Display();
        display.update(new CycleSnapshot(1, 1));
        getInput().forEach(instruction -> state.execute(instruction).forEach(display::update));
        System.out.println("Part 2:\n" + display);
    }

}
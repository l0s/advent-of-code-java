package com.macasaet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 22: Reactor Reboot ---
 */
public class Day22 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-22.txt"),
                        false);
    }

    public record ReactorCore(
            SortedMap<Long, SortedMap<Long, SortedMap<Long, Boolean>>> cubes) {

        public long count(final long xMin, final long xMax, final long yMin, final long yMax, final long zMin, final long zMax) {
            long sum = 0;
            for (var i = xMin; i <= xMax; i++) {
                final var xDimension = cubes().getOrDefault(i, Collections.emptySortedMap());
                for (var j = yMin; j <= yMax; j++) {
                    final var yDimension = xDimension.getOrDefault(j, Collections.emptySortedMap());
                    for (var k = zMin; k <= zMax; k++) {
                        if (yDimension.getOrDefault(k, false)) {
                            sum++;
                        }
                    }
                }
            }
            return sum;
        }

        public void process(final Instruction instruction) {
            final var block = instruction.block();
            final var on = instruction.on();
            for (var i = block.xMin(); i <= block.xMax(); i++) {
                final var xDimension = cubes().computeIfAbsent(i, _key -> new TreeMap<>());
                for (var j = block.yMin(); j <= block.yMax(); j++) {
                    final var yDimension = xDimension.computeIfAbsent(j, _key -> new TreeMap<>());
                    for (var k = block.zMin(); k <= block.zMax(); k++) {
                        yDimension.put(k, on);
                    }
                }
            }
        }

    }

    public record Instruction(boolean on, Block block) {
        public static Instruction parse(final String string) {
            final var components = string.split(" ");
            final boolean on = "on".equalsIgnoreCase(components[0]);
            final var block = Block.parse(components[1]);
            return new Instruction(on, block);
        }

    }

    public record Block(long xMin, long xMax, long yMin, long yMax, long zMin, long zMax) {

        public BigInteger volume() {
            return (BigInteger.valueOf(xMax).subtract(BigInteger.valueOf(xMin)).add(BigInteger.ONE))
                    .multiply(BigInteger.valueOf(yMax).subtract(BigInteger.valueOf(yMin)).add(BigInteger.ONE))
                    .multiply(BigInteger.valueOf(zMax).subtract(BigInteger.valueOf(zMin)).add(BigInteger.ONE));
        }

        public static Block parse(final String string) {
            final var ranges = string.split(",");
            final var xRange = ranges[0].split("\\.\\.");
            final var xMin = Long.parseLong(xRange[0].replaceAll("x=", ""));
            final var xMax = Long.parseLong((xRange[1]));
            final var yRange = ranges[1].split("\\.\\.");
            final var yMin = Long.parseLong((yRange[0].replaceAll("y=", "")));
            final var yMax = Long.parseLong((yRange[1]));
            final var zRange = ranges[2].split("\\.\\.");
            final var zMin = Long.parseLong((zRange[0].replaceAll("z=", "")));
            final var zMax = Long.parseLong((zRange[1]));
            return new Block(xMin, xMax, yMin, yMax, zMin, zMax);
        }

        public boolean overlaps(final Block other) {
            return intersection(other).isPresent();
        }

        public Optional<Block> intersection(final Block other) {
            if (xMin > other.xMax() || xMax < other.xMin()
                    || yMin > other.yMax() || yMax < other.yMin()
                    || zMin > other.zMax() || zMax < other.zMin()) {
                return Optional.empty();
            }
            final var result = new Block(Math.max(xMin, other.xMin()), Math.min(xMax, other.xMax()),
                    Math.max(yMin, other.yMin()), Math.min(yMax, other.yMax()),
                    Math.max(zMin, other.zMin()), Math.min(zMax, other.zMax()));
            return Optional.of(result);
        }
    }

    @Nested
    public class BlockTest {
        @Test
        public final void verifyEqualBlocksOverlap() {
            // given
            final var x = new Block(-2, 2, -2, 2, -2, 2);
            final var y = new Block(-2, 2, -2, 2, -2, 2);

            // when

            // then
            assertTrue(x.overlaps(y));
            assertTrue(y.overlaps(x));
        }

        @Test
        public final void verifyNestedBlocksOverlap() {
            final var inner = new Block(-2, 2, -2, 2, -2, 2);
            final var outer = new Block(-4, 4, -4, 4, -4, 4);

            assertTrue(inner.overlaps(outer));
            assertTrue(outer.overlaps(inner));
        }

        @Test
        public final void verifyIntersectingBlocksOverlap() {
            final var x = new Block(10, 12, 10, 12, 10, 12);
            final var y = new Block(11, 13, 11, 13, 11, 13);

            assertTrue(x.overlaps(y));
            assertTrue(y.overlaps(x));
        }

        @Test
        public final void testIntersection() {
            final var x = new Block(10, 12, 10, 12, 10, 12);
            final var y = new Block(11, 13, 11, 13, 11, 13);

            assertTrue(x.intersection(y).isPresent());
            assertEquals(BigInteger.valueOf(8), x.intersection(y).orElseThrow().volume());
            assertTrue(y.intersection(x).isPresent());
            assertEquals(BigInteger.valueOf(8), y.intersection(x).orElseThrow().volume());
            assertEquals(x.intersection(y).orElseThrow(), y.intersection(x).orElseThrow());
        }
    }

    @Test
    public final void part1() {
        final var core = new ReactorCore(new TreeMap<>());
        getInput().map(Instruction::parse).map(fullInstruction -> {
            final var fullBlock = fullInstruction.block();
            final var truncatedBlock = new Block(Math.max(fullBlock.xMin(), -50), Math.min(fullBlock.xMax(), 50),
                    Math.max(fullBlock.yMin(), -50), Math.min(fullBlock.yMax(), 50),
                    Math.max(fullBlock.zMin(), -50), Math.min(fullBlock.zMax(), 50));
            return new Instruction(fullInstruction.on(), truncatedBlock);
        }).forEach(core::process);
        System.out.println("Part 1: " + core.count(-50, 50, -50, 50, -50, 50));
    }

    @Test
    public final void part2() {
        final var originalList = getInput().map(Instruction::parse).toList();
        final var appliedInstructions = new ArrayList<Instruction>();
        for (final var instruction : originalList) {
            final var modifiedInstructions = new ArrayList<Instruction>();
            if (instruction.on()) {
                // only add initial instructions that turn ON cubes
                modifiedInstructions.add(instruction);
            }
            // override any previous instructions
            for (final var previousInstruction : appliedInstructions) {
                // add compensating instructions to handle the overlaps
                instruction.block()
                        .intersection(previousInstruction.block())
                        .map(intersection -> new Instruction(!previousInstruction.on(), intersection))
                        .ifPresent(modifiedInstructions::add);
            }

            appliedInstructions.addAll(modifiedInstructions);
        }

        final var sum = appliedInstructions.stream()
                .map(instruction -> instruction.block()
                        .volume()
                        .multiply(instruction.on()
                                ? BigInteger.ONE
                                : BigInteger.valueOf(-1)))
                .reduce(BigInteger.ZERO,
                        BigInteger::add);

        System.out.println("Part 2: " + sum);
    }

}
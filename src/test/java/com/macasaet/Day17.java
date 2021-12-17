package com.macasaet;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 17: Trick Shot ---
 */
public class Day17 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-17.txt"),
                        false);
    }

    /**
     * The target area in a large ocean trench
     */
    public record Target(int minX, int maxX, int minY, int maxY) {
    }

    /**
     * A probe at a given point in time
     */
    public record Probe(int xVelocity, int yVelocity, int x, int y) {

        /**
         * Launch a probe from the origin
         *
         * @param xVelocity the starting horizontal velocity
         * @param yVelocity the starting vertical velocity
         * @return the initial state of the probe at the origin
         */
        public static Probe launch(final int xVelocity, final int yVelocity) {
            return new Probe(xVelocity, yVelocity, 0, 0);
        }

        public Optional<Probe> step() {
            if(x > 0 && x + xVelocity < 0) {
                return Optional.empty();
            }
            if(y < 0 && y + yVelocity > 0) {
                return Optional.empty();
            }
            final int newX = x + xVelocity;
            final int newY = y + yVelocity;
            final int newXVelocity = xVelocity > 0
                    ? xVelocity - 1
                    : xVelocity < 0
                    ? xVelocity + 1
                    : xVelocity;
            return Optional.of(new Probe(newXVelocity,
                    yVelocity - 1,
                    newX,
                    newY));
        }

        public Optional<Integer> peak(final Target target) {
            var peak = Integer.MIN_VALUE;
            var p = Optional.of(this);
            while (p.isPresent()) {
                final var probe = p.get();
                peak = Math.max(peak, probe.y());
                if (probe.x() < target.minX() && probe.y() < target.minY()) {
                    // short
                    return Optional.empty();
                } else if (probe.x() > target.maxX()) {
                    // long
                    return Optional.empty();
                } else if (probe.x() >= target.minX() && probe.x() <= target.maxX()
                        && probe.y() >= target.minY() && probe.y() <= target.maxY()) {
                    return Optional.of(peak);
                }
                p = probe.step();
            }
            return Optional.empty();
        }

    }

    @Test
    public final void part1() {
        final var line = getInput().collect(Collectors.toList()).get(0);
        final var bounds = line.replaceFirst("target area: ", "").split(", ");
        final var xBounds = bounds[0].replaceFirst("x=", "").split("\\.\\.");
        final var yBounds = bounds[1].replaceFirst("y=", "").split("\\.\\.");
        final int minX = Integer.parseInt(xBounds[0]);
        final int maxX = Integer.parseInt(xBounds[1]);
        final int minY = Integer.parseInt(yBounds[0]);
        final int maxY = Integer.parseInt(yBounds[1]);
        final var target = new Target(minX, maxX, minY, maxY);

        final var max = IntStream.range(0, 50)
                .parallel()
                .mapToObj(x -> IntStream.range(-50, 50)
                        .parallel()
                        .mapToObj(y -> Probe.launch(x, y))
                ).flatMap(probes -> probes)
                .flatMapToInt(probe -> probe.peak(target)
                        .stream()
                        .mapToInt(peak -> peak))
                .max();


        System.out.println("Part 1: " + max.getAsInt());
    }

    @Test
    public final void part2() {
        final var line = getInput().collect(Collectors.toList()).get(0);
        final var bounds = line.replaceFirst("target area: ", "").split(", ");
        final var xBounds = bounds[0].replaceFirst("x=", "").split("\\.\\.");
        final var yBounds = bounds[1].replaceFirst("y=", "").split("\\.\\.");
        final int minX = Integer.parseInt(xBounds[0]);
        final int maxX = Integer.parseInt(xBounds[1]);
        final int minY = Integer.parseInt(yBounds[0]);
        final int maxY = Integer.parseInt(yBounds[1]);
        final var target = new Target(minX, maxX, minY, maxY);
        int count = 0;
        for (int x = 1; x <= 400; x++) {
            for (int y = -400; y <= 400; y++) {
                final var probe = Probe.launch(x, y);
                if (probe.peak(target).isPresent()) {
                    count++;
                }
            }
        }

        System.out.println("Part 2: " + count);
    }

}
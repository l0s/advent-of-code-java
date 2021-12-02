package com.macasaet;

import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 2: Dive! ---
 */
public class Day02 {

    public enum Operation {
        FORWARD {
            public Position adjust(Position position, int magnitude) {
                return new Position(position.horizontalPosition() + magnitude, position.depth());
            }

            public OrientedPosition adjust(OrientedPosition position, int magnitude) {
                return new OrientedPosition(position.horizontalPosition() + magnitude,
                        position.depth() + (position.aim() * magnitude),
                        position.aim());
            }
        },
        DOWN {
            public Position adjust(Position position, int magnitude) {
                return new Position(position.horizontalPosition(), position.depth() + magnitude);
            }

            public OrientedPosition adjust(OrientedPosition position, int magnitude) {
                return new OrientedPosition(position.horizontalPosition(),
                        position.depth(),
                        position.aim() + magnitude);
            }
        },
        UP {
            public Position adjust(Position position, int magnitude) {
                return new Position(position.horizontalPosition(), position.depth() - magnitude);
            }

            public OrientedPosition adjust(OrientedPosition position, int magnitude) {
                return new OrientedPosition(position.horizontalPosition(),
                        position.depth(),
                        position.aim() - magnitude);
            }
        };

        public abstract Position adjust(Position position, int magnitude);

        public abstract OrientedPosition adjust(OrientedPosition position, int magnitude);
    }

    public record Command(Operation operation, int magnitude) {
        public static Command parse(final String string) {
            final String[] components = string.split(" ");
            final var operation = Operation.valueOf(components[0].toUpperCase(Locale.US));
            final int magnitude = Integer.parseInt(components[1]);
            return new Command(operation, magnitude);
        }

        public Position adjust(final Position position) {
            return operation().adjust(position, magnitude());
        }

        public OrientedPosition adjust(final OrientedPosition position) {
            return operation().adjust(position, magnitude());
        }
    }

    public record Position(int horizontalPosition, int depth) {
        public int result() {
            return horizontalPosition() * depth();
        }
    }

    public record OrientedPosition(int horizontalPosition, int depth, int aim) {
        public int result() {
            return horizontalPosition() * depth();
        }
    }

    protected Stream<Command> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-02.txt"),
                        false)
                .map(Command::parse);
    }

    @Test
    public final void part1() {
        var position = new Position(0, 0);
        for (final var i = getInput().iterator(); i.hasNext(); ) {
            final var operation = i.next();
            position = operation.adjust(position);
        }
        System.out.println("Part 1: " + position.result());
    }

    @Test
    public final void part2() {
        var position = new OrientedPosition(0, 0, 0);
        for (final var i = getInput().iterator(); i.hasNext(); ) {
            final var operation = i.next();
            position = operation.adjust(position);
        }
        System.out.println("Part 2: " + position.result());
    }

}
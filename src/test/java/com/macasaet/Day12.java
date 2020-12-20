package com.macasaet;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day12 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day12.class.getResourceAsStream("/day-12-input.txt"))) {
            // 0, 360 % 360 = North
            // 90 = East
            // 180 = South
            // 270 = West
            int direction = 90;
            int x = 0; // positive means East, negative means West
            int y = 0; // positive means North, negative means South


            final var actions = StreamSupport.stream(spliterator, false)
                    .map(Action::fromLine)
                    .collect(Collectors.toUnmodifiableList());
            for (final var action : actions) {
                switch (action.actionCode) {
                    case 'N':
                        y += action.value;
                        break;
                    case 'S':
                        y -= action.value;
                        break;
                    case 'E':
                        x += action.value;
                        break;
                    case 'W':
                        x -= action.value;
                        break;
                    case 'L':
                        final int relativeDirection = direction - action.value;
                        direction = relativeDirection < 0 ? relativeDirection + 360 : relativeDirection;
                        break;
                    case 'R':
                        direction = (direction + action.value) % 360;
                        break;
                    case 'F':
                        switch (direction) {
                            case 0:
                                y += action.value;
                                break;
                            case 90:
                                x += action.value;
                                break;
                            case 180:
                                y -= action.value;
                                break;
                            case 270:
                                x -= action.value;
                                break;
                            default:
                                throw new IllegalStateException("Unhandled direction: " + direction);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid action: " + action.actionCode);
                }
            }
            System.out.println("Part 1: " + (Math.abs(x) + Math.abs(y)));

            // waypoint coordinates
            int wx = 10; // positive means North, negative means South
            int wy = 1; // positive means East, negative means West

            // ship coordinates
            int sx = 0;
            int sy = 0;

            for (final var action : actions) {
                int xDiff = wx - sx;
                int yDiff = wy - sy;
                switch (action.actionCode) {
                    case 'N':
                        wy += action.value;
                        break;
                    case 'S':
                        wy -= action.value;
                        break;
                    case 'E':
                        wx += action.value;
                        break;
                    case 'W':
                        wx -= action.value;
                        break;
                    case 'L':
                        for (int i = action.value / 90; --i >= 0; ) {
                            wx = sx - yDiff;
                            wy = sy + xDiff;
                            xDiff = wx - sx;
                            yDiff = wy - sy;
                        }
                        break;
                    case 'R':
                        for (int i = action.value / 90; --i >= 0; ) {
                            wx = sx + yDiff;
                            wy = sy - xDiff;
                            xDiff = wx - sx;
                            yDiff = wy - sy;
                        }
                        break;
                    case 'F':
                        sx += xDiff * action.value;
                        sy += yDiff * action.value;
                        wx = sx + xDiff;
                        wy = sy + yDiff;
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported argument: " + action.actionCode);
                }
            }
            System.out.println("Part 2: " + (Math.abs(sx) + Math.abs(sy)));
        }
    }

    protected static class Action {
        final char actionCode;
        final int value;

        public Action(final char actionCode, final int value) {
            this.actionCode = actionCode;
            this.value = value;
        }

        public static Action fromLine(final String line) {
            final var direction = line.charAt(0);
            final var valueString = line.substring(1);
            final int value = Integer.parseInt(valueString);
            return new Action(direction, value);
        }

//        public SimpleNavState navigate(final SimpleNavState current) {
//            switch (actionCode) {
//                case 'N':
//                    return new SimpleNavState(current.x, current.y + value, actionCode);
//                case 'S':
//                    return new SimpleNavState(current.x, current.y - value, actionCode);
//                case 'E':
//                    return new SimpleNavState(current.x + value, current.y, actionCode);
//                case 'W':
//                    return new SimpleNavState(current.x - value, current.y, actionCode);
//                case 'L':
//                    final int relativeDirection = current.direction - value;
//                    final int direction = relativeDirection < 0 ? relativeDirection + 360 : relativeDirection;
//                    return new SimpleNavState(current.x, current.y, direction);
//                case 'R':
//                    return new SimpleNavState(current.x, current.y, (current.direction + value) % 360);
//                case 'F':
//                    switch (current.direction) {
//                        case 0:
//                            return new SimpleNavState(current.x, current.y + value, current.direction);
//                        case 90:
//                            return new SimpleNavState(current.x + value, current.y, current.direction);
//                        case 180:
//                            return new SimpleNavState(current.x, current.y - value, current.direction);
//                        case 270:
//                            return new SimpleNavState(current.x - value, current.y, current.direction);
//                        default:
//                            throw new IllegalStateException("Unhandled direction: " + current.direction);
//                    }
//                default:
//                    throw new IllegalArgumentException("Invalid action: " + current.direction);
//            }
//        }

    }
//
//    protected static class SimpleNavState {
//        private final int x;
//        private final int y;
//        private final int direction;
//
//        public SimpleNavState(int x, int y, int direction) {
//            this.x = x;
//            this.y = y;
//            this.direction = direction;
//        }
//    }
//
//    protected static class AdvancedNavState {
//        private final int sx;
//        private final int sy;
//        private final int wx;
//
//        public AdvancedNavState(int sx, int sy, int wx, int wy) {
//            this.sx = sx;
//            this.sy = sy;
//            this.wx = wx;
//            this.wy = wy;
//        }
//
//        private final int wy;
//    }
}
package com.macasaet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 23: Amphipod ---
 */
public class Day23 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-23.txt"),
                        false);
    }

    protected Tile[][] parseGrid(List<String> lines) {
        final var result = new Tile[lines.size()][];
        for (int i = lines.size(); --i >= 0; ) {
            final var line = lines.get(i);
            result[i] = new Tile[line.length()];
            for (int j = line.length(); --j >= 0; ) {
                final AmphipodType targetType = AmphipodType.forDestinationColumn(j);
                final var c = line.charAt(j);
                result[i][j] = switch (c) {
                    case '.' -> new Tile(new Point(i, j), null, null);
                    case 'A' -> new Tile(new Point(i, j), targetType, AmphipodType.AMBER);
                    case 'B' -> new Tile(new Point(i, j), targetType, AmphipodType.BRONZE);
                    case 'C' -> new Tile(new Point(i, j), targetType, AmphipodType.COPPER);
                    case 'D' -> new Tile(new Point(i, j), targetType, AmphipodType.DESERT);
                    default -> null;
                };
            }
        }

        return result;
    }

    public enum AmphipodType {
        AMBER(1, 3),
        BRONZE(10, 5),
        COPPER(100, 7),
        DESERT(1000, 9);

        private final int energyPerStep;
        private final int destinationColumn;

        AmphipodType(final int energyPerStep, final int destinationColumn) {
            this.energyPerStep = energyPerStep;
            this.destinationColumn = destinationColumn;
        }

        public static AmphipodType forDestinationColumn(final int destinationColumn) {
            for (final var candidate : values()) {
                if (candidate.destinationColumn == destinationColumn) {
                    return candidate;
                }
            }
            return null;
        }
    }

    public record Move(Point from, Point to) {
    }

    public record BranchResult(Node node, int cost) {
    }

    public record Node(Tile[][] tiles, Map<List<Move>, BranchResult> branchCache) {

        private static final Map<Node, Integer> estimatedDistanceCache = new ConcurrentHashMap<>();
        private static final Map<Node, List<BranchResult>> branchesCache = new ConcurrentHashMap<>();
        private static final Map<Node, Boolean> solutionCache = new ConcurrentHashMap<>();

        public static Node createInitialNode(final Tile[][] tiles) {
            return new Node(tiles, new ConcurrentHashMap<>());
        }

        public BranchResult branch(final List<Move> moves) {
            if (moves.size() == 0) {
                System.err.println("How is this empty?");
                return new BranchResult(this, 0);
            }
            if (branchCache.containsKey(moves)) {
                return branchCache.get(moves);
            }
            final var copy = new Tile[tiles.length][];
            for (int i = tiles.length; --i >= 0; ) {
                final var row = new Tile[tiles[i].length];
                System.arraycopy(tiles[i], 0, row, 0, tiles[i].length);
                copy[i] = row;
            }
            final var source = moves.get(0).from();
            final var destination = moves.get(moves.size() - 1).to();
            final var sourceTile = source.getTile(tiles);
            final var destinationTile = destination.getTile(tiles);
            final var amphipod = sourceTile.amphipodType();
            if (amphipod == null) {
                System.err.println("source amphipod is missing :-(");
            }
            source.setTile(copy, sourceTile.updateType(null));
            destination.setTile(copy, destinationTile.updateType(amphipod));
            final var cost = moves.size() * amphipod.energyPerStep;
            final var result = new BranchResult(new Node(copy, new ConcurrentHashMap<>()), cost);
            branchCache.put(moves, result);
            return result;
        }

        boolean isSideRoom(final Point point) {
            final var x = point.x();
            final var y = point.y();
            return x > 1 && (y == 3 || y == 5 || y == 7 || y == 9);
        }

        boolean isCorridor(final Point point) {
            return point.x() == 1;
        }

        public int hashCode() {
            // equality based on layout of the burrow regardless of how the amphipods got to that state
            // FNV hash
            long result = 2166136261L;
            final Function<Tile[], Long> rowHasher = row -> {
                long rowHash = 2166136261L;
                for (final var tile : row) {
                    rowHash = (16777619L * rowHash) ^ (tile == null ? 0L : (long) Objects.hashCode(tile.amphipodType()));
                }
                return rowHash;
            };
            for (final var row : tiles()) {
                result = (16777619L * result) ^ rowHasher.apply(row);
            }

            return Long.hashCode(result);
            // Bob Jenkins' One-at-a-Time hash
//            int result = 0;
//            final Function<Tile[], Integer> rowHasher = row -> {
//                int rowHash = 0;
//                for(final var tile : row) {
//                    final var tileHash = tile != null ? Objects.hashCode(tile.amphipodType()) : 0;
//                    rowHash += tileHash;
//                    rowHash += rowHash << 10;
//                    rowHash ^= rowHash >> 6;
//                }
//                rowHash += rowHash << 3;
//                rowHash ^= rowHash >> 11;
//                rowHash += rowHash << 15;
//                return rowHash;
//            };
//            for(final var row : tiles()) {
//                result += rowHasher.apply(row);
//                result += (result << 10);
//                result ^= (result >> 6);
//            }
//            result += result << 3;
//            result ^= result >> 11;
//            result += result << 15;
//            return result;
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            if (this == o) {
                return true;
            }
            try {
                final var other = (Node) o;
                // equality based on layout of the burrow regardless of how the amphipods got to that state
                if (tiles().length != other.tiles().length) {
                    return false;
                }
                for (int i = tiles().length; --i >= 0; ) {
                    final var xRow = tiles()[i];
                    final var yRow = other.tiles()[i];
                    if (xRow.length != yRow.length) {
                        return false;
                    }
                    for (int j = xRow.length; --j >= 0; ) {
                        if (xRow[j] != yRow[j]) {
                            if (xRow[j] == null
                                    || yRow[j] == null
                                    || !Objects.equals(xRow[j].amphipodType(), yRow[j].amphipodType())) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            } catch (final ClassCastException cce) {
                return false;
            }
        }

        public String toString() {
            final var builder = new StringBuilder();
            for (final var row : tiles()) {
                for (final var cell : row) {
                    if (cell == null) {
                        builder.append('#');
                    } else if (cell.amphipodType() == null) {
                        builder.append('.');
                    } else {
                        builder.append(switch (cell.amphipodType()) {
                            case AMBER -> 'A';
                            case BRONZE -> 'B';
                            case COPPER -> 'C';
                            case DESERT -> 'D';
                        });
                    }
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        public boolean isSolution() {
            if (solutionCache.containsKey(this)) {
                return solutionCache.get(this);
            }
            final var result = Arrays.stream(tiles())
                    .flatMap(Arrays::stream)
                    .filter(Objects::nonNull)
                    .allMatch(Tile::hasTargetType);
            solutionCache.put(this, result);
            return result;
        }

        public Stream<BranchResult> getBranches() {
            if (branchesCache.containsKey(this)) {
                return branchesCache.get(this).stream();
            }
            final var branchResults = new Vector<BranchResult>();
            return Arrays.stream(tiles())
                    .parallel()
                    .flatMap(row -> Arrays.stream(row)
                            .parallel()
                            .filter(Objects::nonNull)
                            .filter(tile -> !tile.isVacant())
                            .flatMap(this::getMoves))
                    .map(this::branch)
                    .peek(branchResults::add)
                    .onClose(() -> branchesCache.put(this, Collections.unmodifiableList(branchResults)));
        }

//        @Deprecated
//        public Set<List<Move>> getMoves() {
//            final var result = new HashSet<List<Move>>();
//            for (final var row : tiles()) {
//                for (final var tile : row) {
//                    if (tile != null && !tile.isVacant()) {
//                        result.addAll(getMoves(tile).collect(Collectors.toSet()));
//                    }
//                }
//            }
//            return Collections.unmodifiableSet(result);
//        }

        /**
         * Find all the actions (series of moves) that can be taken for a single amphipod.
         *
         * @param occupiedTile a tile with an amphipod
         * @return All the moves that can be applied starting with <em>occupiedTile</em> that end in a valid temporary
         * destination for the amphipod
         */
        Stream<List<Move>> getMoves(final Tile occupiedTile) {
            if (isSideRoom(occupiedTile.location()) && occupiedTile.hasTargetType()) {
                var roomComplete = true;
                for (var cursor = tiles[occupiedTile.location().x() + 1][occupiedTile.location().y()];
                     cursor != null;
                     cursor = tiles[cursor.location().x() + 1][cursor.location().y()]) {
                    if (!cursor.hasTargetType()) {
                        // one of the amphipods in this room is destined elsewhere
                        // so the amphipod from the original tile will need to move out of the way
                        roomComplete = false;
                        break;
                    }
                }
                if (roomComplete) {
                    // all the amphipods in this room have this as their intended destination
                    return Stream.empty();
                }
            }

            var paths = iterateThroughPaths(occupiedTile.amphipodType(),
                    occupiedTile,
                    Collections.singletonList(occupiedTile.location()));
            if (isCorridor(occupiedTile.location())) {
                /*
                 * "Once an amphipod stops moving in the hallway, it will stay in that spot until it can move into a
                 * room. (That is, once any amphipod starts moving, any other amphipods currently in the hallway are
                 * locked in place and will not move again until they can move fully into a room.)"
                 */
                paths = paths
                        // only select paths that end in a room
                        .filter(path -> isSideRoom(path.get(path.size() - 1)
                                .getTile(tiles())
                                .location()));
            } else {
                // reduce the search space by only considering rooms from rooms into hallways
                // prune any path that starts from a room and ends in a room
//                paths = paths
//                        .filter(path -> isCorridor(path.get(path.size() - 1)
//                                .getTile(tiles())
//                                .location()));
            }
            // convert tiles to moves
            return paths
                    .filter(path -> path.size() > 1) // filter out paths in which the amphipod does not move
                    .map(points -> {
                        final var moves = new ArrayList<Move>(points.size() - 1);
                        for (int i = 1; i < points.size(); i++) {
                            moves.add(new Move(points.get(i - 1), points.get(i)));
                        }
                        return Collections.unmodifiableList(moves);
                    });
        }

        Stream<List<Point>> iterateThroughPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
            // TODO store `pathSoFar` as a stack so checking for node becomes O(1) instead of O(n)
            final int x = current.location.x();
            final int y = current.location.y();
            final var up = tiles[x - 1][y];
            final var down = tiles[x + 1][y];
            final var left = tiles[x][y - 1];
            final var right = tiles[x][y + 1];
            final var suppliers = new ArrayList<Supplier<Stream<List<Point>>>>(4);
            if (up != null && up.isVacant() && !pathSoFar.contains(up.location())) {
                suppliers.add(() -> streamUpPaths(amphipodType, current, pathSoFar));
            }
            if (down != null
                    && down.isVacant()
                    && !pathSoFar.contains(down.location())
                    // don't enter side room unless it is the ultimate destination
                    && down.targetType == amphipodType) {
                suppliers.add(() -> streamDownPaths(amphipodType, current, pathSoFar));
            }
            if (left != null && left.isVacant() && !pathSoFar.contains(left.location())) {
                suppliers.add(() -> streamLeftPaths(amphipodType, current, pathSoFar));
            }
            if (right != null && right.isVacant() && !pathSoFar.contains(right.location())) {
                suppliers.add(() -> streamRightPaths(amphipodType, current, pathSoFar));
            }
            if (suppliers.isEmpty()) {
                // dead end, emit the path so far
                suppliers.add(() -> Stream.of(Collections.unmodifiableList(pathSoFar)));
            }

            return suppliers.stream()
                    .flatMap(Supplier::get);
        }

        Stream<List<Point>> streamUpPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
            final int x = current.location.x();
            final int y = current.location.y();
            final var up = tiles[x - 1][y];
            // amphipod is in a side room
            if (isSideRoom(up.location()) && current.targetType == amphipodType) {
                // amphipod is in the back of the room in which it belongs, stop here
                return Stream.of(pathSoFar);
            }
            final var incrementalPath = new ArrayList<>(pathSoFar);
            incrementalPath.add(up.location());
            // whether "up" is the front of the room or the corridor outside the room, we have to keep moving
            return iterateThroughPaths(amphipodType, up, incrementalPath);
        }

        Stream<List<Point>> streamDownPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
            final int x = current.location.x();
            final int y = current.location.y();
            final var down = tiles[x + 1][y];
            final var incrementalPath = new ArrayList<>(pathSoFar);
            incrementalPath.add(down.location());
            if ((isCorridor(current.location()) && canEnterRoom(amphipodType, down)) || isSideRoom(current.location())) {
                // go as for back into the room as possible, don't just stop at the entrance
                return iterateThroughPaths(amphipodType, down, incrementalPath);
            }
            return Stream.empty();
        }

        Stream<List<Point>> streamLeftPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
            final int x = current.location.x();
            final int y = current.location.y();
            final var left = tiles[x][y - 1];
            Stream<List<Point>> result = Stream.empty();
            final var incrementalPath = new ArrayList<>(pathSoFar);
            incrementalPath.add(left.location());
            if (tiles[left.location().x() + 1][left.location().y()] == null || !isSideRoom(tiles[left.location().x() + 1][left.location().y()].location())) {
                // this is not in front of a side room,
                // we can stop here while other amphipods move
                result = Stream.concat(result, Stream.of(Collections.unmodifiableList(incrementalPath)));
            }
            result = Stream.concat(result, iterateThroughPaths(amphipodType, left, incrementalPath));
            return result;
        }

        Stream<List<Point>> streamRightPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
            final int x = current.location.x();
            final int y = current.location.y();
            final var right = tiles[x][y + 1];
            Stream<List<Point>> result = Stream.empty();
            final var incrementalPath = new ArrayList<>(pathSoFar);
            incrementalPath.add(right.location());
            if (tiles[right.location().x() + 1][right.location().y()] == null || !isSideRoom(tiles[right.location().x() + 1][right.location().y()].location())) {
                // this is not in front of a side room,
                // we can stop here while other amphipods move
                result = Stream.concat(result, Stream.of(Collections.unmodifiableList(incrementalPath)));
            }
            result = Stream.concat(result, iterateThroughPaths(amphipodType, right, incrementalPath));
            return result;
        }

//        /**
//         * Find all the paths an amphipod can take
//         *
//         * @param amphipodType the type of amphipod that is moving
//         * @param current      a tile through which the amphipod will take
//         * @param pathSoFar    the full path of the amphipod so far, *must* include _current_
//         * @return all the paths (start to finish) the amphipod can take
//         */
//        @Deprecated
//        List<List<Point>> getPaths(final AmphipodType amphipodType, final Tile current, final List<Point> pathSoFar) {
//            final int x = current.location.x();
//            final int y = current.location.y();
//            final var up = tiles[x - 1][y];
//            final var down = tiles[x + 1][y];
//            final var left = tiles[x][y - 1];
//            final var right = tiles[x][y + 1];
//
//            final var result = new ArrayList<List<Point>>();
//            if (up != null && up.isVacant() && !pathSoFar.contains(up.location())) {
//                // amphipod is in a side room
//                if (isSideRoom(up.location()) && current.targetType == amphipodType) {
//                    // amphipod is in the back of the room in which it belongs, stop here
//                    return Collections.singletonList(pathSoFar);
//                }
//                final var incrementalPath = new ArrayList<>(pathSoFar);
//                incrementalPath.add(up.location());
//                // whether "up" is the front of the room or the corridor outside the room, we have to keep moving
//                result.addAll(getPaths(amphipodType, up, incrementalPath));
//            }
//            if (down != null
//                    && down.isVacant()
//                    && !pathSoFar.contains(down.location())
//                    // don't enter side room unless it is the ultimate destination
//                    && down.targetType == amphipodType) {
//                // either entering a room or moving to the back of the room
//                final var incrementalPath = new ArrayList<>(pathSoFar);
//                incrementalPath.add(down.location());
//                if ((isCorridor(current.location()) && canEnterRoom(amphipodType, down)) || isSideRoom(current.location())) {
//                    // go as for back into the room as possible, don't just stop at the entrance
//                    result.addAll(getPaths(amphipodType, down, incrementalPath));
//                }
//            }
//            if (left != null && left.isVacant() && !pathSoFar.contains(left.location())) {
//                final var incrementalPath = new ArrayList<>(pathSoFar);
//                incrementalPath.add(left.location());
//                if (tiles[left.location().x() + 1][left.location().y()] == null || !isSideRoom(tiles[left.location().x() + 1][left.location().y()].location())) {
//                    // this is not in front of a side room,
//                    // we can stop here while other amphipods move
//                    result.add(Collections.unmodifiableList(incrementalPath));
//                }
//                result.addAll(getPaths(amphipodType, left, incrementalPath));
//            }
//            if (right != null && right.isVacant() && !pathSoFar.contains(right.location())) {
//                final var incrementalPath = new ArrayList<>(pathSoFar);
//                incrementalPath.add(right.location());
//                if (tiles[right.location().x() + 1][right.location().y()] == null || !isSideRoom(tiles[right.location().x() + 1][right.location().y()].location())) {
//                    // this is not in front of a side room,
//                    // we can stop here while other amphipods move
//                    result.add(Collections.unmodifiableList(incrementalPath));
//                }
//                result.addAll(getPaths(amphipodType, right, incrementalPath));
//            }
//            if (result.isEmpty() && pathSoFar.size() > 1) {
//                // dead end, emit the path so far
//                result.add(pathSoFar);
//            }
//            return Collections.unmodifiableList(result);
//        }

        boolean canEnterRoom(final AmphipodType amphipodType, final Tile frontOfRoom) {
            if (!isSideRoom(frontOfRoom.location())) {
                throw new IllegalArgumentException("Not a side room: " + frontOfRoom);
            }
            if (frontOfRoom.targetType() != amphipodType) {
                // this is not the destination room
                return false;
            }
            // ensure all occupants have this as their destination
            boolean hasOccupants = false;
            for (var roomTile = tiles()[frontOfRoom.location().x() + 1][frontOfRoom.location().y()];
                 roomTile != null;
                 roomTile = tiles()[roomTile.location().x() + 1][roomTile.location().y()]) {
                if (roomTile.amphipodType() == null) {
                    if (hasOccupants) {
                        System.err.println("***There is a gap in the room***");
                        return false; // there is a gap that shouldn't be here
                    }
                    continue;
                } else {
                    hasOccupants = true;
                }
                if (!roomTile.hasTargetType()) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Estimate the energy required for all the amphipods to get to their side rooms. This strictly underestimates
         * the amount of energy required. It assumes: each amphipod has an unobstructed path to their room, they only
         * need to get to the front of the room, not all the way to the back, they do not need to take any detours to
         * let other amphipods pass (e.g. they don't need to leave the room and then come back in).
         *
         * @return an underestimate of the energy required for the amphipods of the burrow to self-organise
         */
        public int estimatedDistanceToSolution() {
            if (estimatedDistanceCache.containsKey(this)) {
                return estimatedDistanceCache.get(this);
            }
            int result = 0;
            for (int i = tiles.length; --i >= 0; ) {
                final var row = tiles[i];
                for (int j = row.length; --j >= 0; ) {
                    final var tile = row[j];
                    if (tile != null && tile.amphipodType != null) {
                        final int horizontalDistance =
                                Math.abs(tile.amphipodType.destinationColumn - tile.location().y());
                        int verticalDistance = 0;
                        if (horizontalDistance != 0) {
                            // get to the corridor
                            verticalDistance = tile.location().x() - 1;
                            // enter the side room
                            verticalDistance += 1;
                        } else if (isCorridor(tile.location())) {
                            // it's implied that horizontal distance is 0
                            // we're right outside the target room
                            // enter the side room
                            verticalDistance = 1;
                        }
                        final int distance = verticalDistance + horizontalDistance;
                        result += distance * tile.amphipodType().energyPerStep;
                    }
                }
            }
            estimatedDistanceCache.put(this, result);
            return result;
        }

    }

    protected record Point(int x, int y) {

        public Tile getTile(final Tile[][] tiles) {
            return tiles[x()][y()];
        }

        public void setTile(final Tile[][] tiles, final Tile tile) {
            if (tile.location().x() != x() || tile.location().y() != y()) {
                throw new IllegalArgumentException("Tile and location do not match");
            }
            tiles[x()][y()] = tile;
        }
    }

    public record Tile(Point location, AmphipodType targetType, AmphipodType amphipodType) {

        public Tile updateType(final AmphipodType newType) {
            return new Tile(location, targetType, newType);
        }

        public boolean isVacant() {
            return amphipodType == null;
        }

        public boolean hasTargetType() {
            return Objects.equals(amphipodType(), targetType());
        }

    }

//    public int lwst(final Node start) {
//        final var lowestCostToNode = new ConcurrentHashMap<Node, Integer>();
//        final var estimatedCostThroughNode = new ConcurrentHashMap<Node, Integer>();
//        final var openSet = new PriorityBlockingQueue<Node>(100000, Comparator.comparing(estimatedCostThroughNode::get));
//
//        // add the starting node, getting there is free
//        lowestCostToNode.put(start, 0);
//        estimatedCostThroughNode.put(start, start.estimatedDistanceToSolution());
//        openSet.add(start);
//
//        final var executor = ForkJoinPool.commonPool();
//        final var stateModifiers = new LinkedBlockingDeque<Supplier<Integer>>();
//        final var complete = new AtomicBoolean(false);
//        executor.execute(() -> {
//            while (!complete.get()) {
//                Thread.yield();
//                try {
//                    final var current = openSet.take();
//                    if (current.isSolution()) {
//                        stateModifiers.addFirst(() -> lowestCostToNode.get(current));
//                    }
//                    final var lowestCostToCurrent = lowestCostToNode.get(current);
//                    executor.execute(() -> current.getBranches().map(branchResult -> {
//                        final var tentativeBranchCost = lowestCostToCurrent + branchResult.cost();
//                        final var branchNode = branchResult.node();
//                        final Supplier<Integer> updater = () -> {
//                            if (tentativeBranchCost < lowestCostToNode.getOrDefault(branchNode, Integer.MAX_VALUE)) {
//                                // either we've never visited this node before,
//                                // or the last time we did, we took a more expensive route
//                                lowestCostToNode.put(branchNode, tentativeBranchCost);
//
//                                // update the cost through this branch
//                                // need to remove and re-add to get correct ordering in the open set
//                                estimatedCostThroughNode.put(branchNode, tentativeBranchCost + branchNode.estimatedDistanceToSolution());
//
//                                openSet.remove(branchNode); // O(n)
//                                openSet.add(branchNode); // O(log(n))
//                            }
//                            return (Integer)null;
//                        };
//                        return updater;
//                    }).forEach(stateModifiers::addLast));
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                    complete.set(true);
//                    Thread.currentThread().interrupt();
//                    throw new RuntimeException(e.getMessage(), e);
//                }
//            }
//        });
//        // process updates sequentially
//        while (!complete.get()) {
//            Thread.yield();
//            try {
//                final var updater = stateModifiers.takeFirst();
//                final var cost = updater.get();
//                if (cost != null) {
//                    complete.set(true);
//                    return cost;
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//                complete.set(true);
//                Thread.currentThread().interrupt();
//                throw new RuntimeException(e.getMessage(), e);
//            }
//        }
//        throw new IllegalStateException("An error occurred");
//    }
//
//    protected String id(Node node) {
//        final var outputStream = new ByteArrayOutputStream();
//        outputStream.write(node.hashCode());
//        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
//    }

    public int lowest(final Node start) {
        final var lowestCostToNode = new ConcurrentHashMap<Node, Integer>();
        final var estimatedCostThroughNode = new ConcurrentHashMap<Node, Integer>();
        final var openSet = new PriorityBlockingQueue<Node>(100000, Comparator.comparing(estimatedCostThroughNode::get));

        // add the starting node, getting there is free
        lowestCostToNode.put(start, 0);
        estimatedCostThroughNode.put(start, start.estimatedDistanceToSolution());
        openSet.add(start);

        while (!openSet.isEmpty()) {
            if(Node.solutionCache.size() % 10000 == 0) {
                System.err.println(openSet.size() + " branches left to check in the open set");
                System.err.println("Appraised the energy cost to " + lowestCostToNode.size() + " nodes.");
                System.err.println("Estimated the energy cost through " + estimatedCostThroughNode.size() + " nodes.");
                System.err.println("Lowest estimated cost so far: " + estimatedCostThroughNode.get(openSet.peek()));
            }
            final var current = openSet.poll(); // O(log(n))
            if (current.isSolution()) {
                System.err.println("Found solution:\n" + current);
                return lowestCostToNode.get(current);
            }
            final var lowestCostToCurrent = lowestCostToNode.get(current);
            current.getBranches()
                    .parallel()
                    .filter(branchResult -> {
                        final var tentativeBranchCost = lowestCostToCurrent + branchResult.cost();
                        final var branchNode = branchResult.node();
                        return tentativeBranchCost < lowestCostToNode.getOrDefault(branchNode, Integer.MAX_VALUE);
                    })
                    .forEach(branchResult -> {
                        final var tentativeBranchCost = lowestCostToCurrent + branchResult.cost();
                        final var branchNode = branchResult.node();
                        // either we've never visited this node before,
                        // or the last time we did, we took a more expensive route
                        lowestCostToNode.put(branchNode, tentativeBranchCost);

                        // update the cost through this branch
                        // need to remove and re-add to get correct ordering in the open set
//                        openSet.remove(branchNode); // O(n)
                        openSet.removeIf(node -> node.equals(branchNode));
                        estimatedCostThroughNode.put(branchNode, tentativeBranchCost + branchNode.estimatedDistanceToSolution());
                        openSet.add(branchNode); // O(log(n))
                    });
//            current.getBranches().forEach(branchResult -> {
//                final var tentativeBranchCost = lowestCostToCurrent + branchResult.cost();
//                final var branchNode = branchResult.node();
//                if (tentativeBranchCost < lowestCostToNode.getOrDefault(branchNode, Integer.MAX_VALUE)) {
//                    // either we've never visited this node before,
//                    // or the last time we did, we took a more expensive route
//                    lowestCostToNode.put(branchNode, tentativeBranchCost);
//
//                    // update the cost through this branch
//                    // need to remove and re-add to get correct ordering in the open set
//                    openSet.remove(branchNode); // O(n)
//                    estimatedCostThroughNode.put(branchNode, tentativeBranchCost + branchNode.estimatedDistanceToSolution());
//                    openSet.add(branchNode); // O(log(n))
//                }
//            });
        }
        throw new IllegalStateException("Amphipods are gridlocked :-(");
    }

    @Nested
    public class NodeTest {

        @Test
        public final void verifyEquality() {
            // given
            final var string = """
                    #############
                    #.....D.D.A.#
                    ###.#B#C#.###
                      #A#B#C#.#
                      #########
                    """;
            final var x = Node.createInitialNode(parseGrid(string.lines().toList()));
            final var y = Node.createInitialNode(parseGrid(string.lines().toList()));

            // when

            // then
            assertEquals(x.hashCode(), y.hashCode());
            assertEquals(x, y);
        }

        @Test
        public final void verifyBranchEquality() {
            // given
            final var string = """
                    #############
                    #.....D.D.A.#
                    ###.#B#C#.###
                      #A#B#C#.#
                      #########
                    """;
            final var original = Node.createInitialNode(parseGrid(string.lines().toList()));

            // when
            final var x = original.branch(Collections.singletonList(new Move(new Point(2, 5), new Point(1, 2))));
            final var y = original.branch(Collections.singletonList(new Move(new Point(2, 5), new Point(1, 2))));

            // then
            assertEquals(x.hashCode(), y.hashCode());
            assertEquals(x, y);
        }

        @Test
        public final void verifyEstimatedDistanceIsZero() {
            // given
            final var string = """
                    #############
                    #...........#
                    ###A#B#C#D###
                      #A#B#C#D#
                      #########
                      """;
            final var initial = Node.createInitialNode(parseGrid(string.lines().toList()));

            // when
            final var result = initial.estimatedDistanceToSolution();

            // then
            assertEquals(0, result);
        }

        @Test
        public final void verifyEstimationOrdering() {
            // given
            final var states = new String[]{
                    """
                    #############
                    #...........#
                    ###B#C#B#D###
                      #A#D#C#A#
                      #########
                    """,
                    """
                    #############
                    #...B.......#
                    ###B#C#.#D###
                      #A#D#C#A#
                      #########
                    """,
                    """
                    #############
                    #...B.......#
                    ###B#.#C#D###
                      #A#D#C#A#
                      #########
                    """,
                    """
                    #############
                    #.....D.....#
                    ###B#.#C#D###
                      #A#B#C#A#
                      #########
                    """,
                    """
                    #############
                    #.....D.....#
                    ###.#B#C#D###
                      #A#B#C#A#
                      #########
                    """,
                    """
                    #############
                    #.....D.D.A.#
                    ###.#B#C#.###
                      #A#B#C#.#
                      #########
                    """,
                    """
                    #############
                    #.........A.#
                    ###.#B#C#D###
                      #A#B#C#D#
                      #########
                    """,
                    """
                    #############
                    #...........#
                    ###A#B#C#D###
                      #A#B#C#D#
                      #########
                    """
            };
            final var nodes = Arrays.stream(states)
                    .map(String::lines)
                    .map(Stream::toList)
                    .map(Day23.this::parseGrid)
                    .map(Node::createInitialNode)
                    .toList();

            // when

            // then
            for (int i = 1; i < nodes.size(); i++) {
                final var previous = nodes.get(i - 1);
                final var current = nodes.get(i);
                assertTrue(previous.estimatedDistanceToSolution() >= current.estimatedDistanceToSolution(),
                        "Previous state has a lower estimated distance. Previous:\n" + previous + "\n(cost: " + previous.estimatedDistanceToSolution() + ")\nCurrent:\n" + current + "\n(cost: " + current.estimatedDistanceToSolution() + ")");
            }
        }
    }

    @Test
    public final void part1() {
        final var initial = Node.createInitialNode(parseGrid(getInput().toList()));

        System.out.println("Part 1: " + lowest(initial));
    }

    @Test
    public final void part2() {
        final var lines = getInput().collect(Collectors.toList());
        lines.add(3, "  #D#B#A#C#  ");
        lines.add(3, "  #D#C#B#A#  ");
        final var initial = Node.createInitialNode(parseGrid(lines));
        System.err.println("Initial state:\n" + initial);

        System.out.println("Part 2: " + lowest(initial));
    }

}
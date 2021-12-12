package com.macasaet;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

/**
 * --- Day 12: Passage Pathing ---
 */
public class Day12 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-12.txt"),
                        false);
    }

    /**
     * @return a map of the connected caves
     */
    protected Map<String, Node> getMap() {
        final var map = new HashMap<String, Node>();
        getInput().forEach(line -> {
            final var components = line.split("-");
            final var sourceLabel = components[0];
            final var targetLabel = components[1];
            final var source = map.computeIfAbsent(sourceLabel, Node::new);
            final var target = map.computeIfAbsent(targetLabel, Node::new);
            source.connected.add(target);
            target.connected.add(source);
        });
        return Collections.unmodifiableMap(map);
    }

    public Node getStartingPoint() {
        return getMap().get("start");
    }

    /**
     * A distinct path through the cave system
     */
    public record Path(List<Node> nodes, Node specialCave, int specialCaveVisits) {

        public int hashCode() {
            int result = 0;
            for (final var node : nodes()) {
                result = result * 31 + node.hashCode();
            }
            return result;
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            try {
                final var other = (Path) o;
                return nodes().equals(other.nodes());
            } catch (final ClassCastException cce) {
                return false;
            }
        }
    }

    public static class Node {
        private final boolean isStart;
        private final boolean isEnd;
        private final boolean isSmallCave;
        private final String label;

        private final Set<Node> connected = new HashSet<>();

        public Node(final String label) {
            this("start".equalsIgnoreCase(label), "end".equalsIgnoreCase(label),
                    label.toLowerCase(Locale.ROOT).equals(label), label);
        }

        protected Node(boolean isStart, boolean isEnd, boolean isSmallCave, final String label) {
            this.isStart = isStart;
            this.isEnd = isEnd;
            this.isSmallCave = isSmallCave;
            this.label = label;
        }

        public int hashCode() {
            int result = 0;
            result += result * 31 + label.hashCode();
            return result;
        }

        public boolean equals(final Object o) {
            if (o == null) {
                return false;
            }
            try {
                final Node other = (Node) o;
                return label.equals(other.label);
            } catch (final ClassCastException cce) {
                return false;
            }
        }

    }

    protected Set<Path> getPaths(final Node node, final Path pathSoFar) {
        final var result = new HashSet<Path>();

        if (node.isStart && pathSoFar.nodes.size() > 1) {
            // "once you leave the start cave, you may not return to it"
            return Collections.emptySet();
        }

        final var nodes = new ArrayList<>(pathSoFar.nodes());
        if (node.isEnd) {
            // "once you reach the end cave, the path must end immediately"
            nodes.add(node);
            return Collections.singleton(new Path(Collections.unmodifiableList(nodes), pathSoFar.specialCave(), pathSoFar.specialCaveVisits()));
        }
        int specialCaveVisits = pathSoFar.specialCaveVisits();
        if (node.isSmallCave) {
            if (node.equals(pathSoFar.specialCave())) {
                // "a single small cave can be visited at most twice"
                if (pathSoFar.specialCaveVisits() < 1) {
                    specialCaveVisits++;
                } else {
                    return Collections.emptySet();
                }
            } else {
                if (pathSoFar.nodes().contains(node)) {
                    // "the remaining small caves can be visited at most once"
                    return Collections.emptySet();
                }
            }
        }
        nodes.add(node);
        for (final var neighbour : node.connected) {
            if (neighbour.isSmallCave && pathSoFar.specialCave() == null) {
                result.addAll(getPaths(neighbour, new Path(Collections.unmodifiableList(nodes), null, 0)));
                result.addAll(getPaths(neighbour, new Path(Collections.unmodifiableList(nodes), neighbour, 0)));
            } else {
                result.addAll(getPaths(neighbour, new Path(Collections.unmodifiableList(nodes), pathSoFar.specialCave(), specialCaveVisits)));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    protected int countPaths(final Node node, final Set<Node> visitedSmallCaves) {
        int result = 0;
        if (node.isEnd) {
            return 1;
        }
        if (visitedSmallCaves.contains(node)) {
            // invalid path
            return 0;
        }
        if (node.isSmallCave) {
            visitedSmallCaves.add(node);
        }
        for (final var connected : node.connected) {
            final var set = new HashSet<>(visitedSmallCaves);
            result += countPaths(connected, set);
        }
        return result;
    }

    @Test
    public final void part1() {
        final var start = getStartingPoint();
        final int result = countPaths(start, new HashSet<>());
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var start = getStartingPoint();
        final var paths = getPaths(start, new Path(Collections.emptyList(), null, 0));
        System.out.println("Part 2: " + paths.size());
    }

}
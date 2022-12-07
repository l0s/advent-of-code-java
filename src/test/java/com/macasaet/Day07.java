package com.macasaet;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * --- Day 7: ---
 * <a href="https://adventofcode.com/2022/day/7">https://adventofcode.com/2022/day/7</a>
 */
public class Day07 {

    static class Session {
        private final Directory root = new Directory("/", new HashMap<>());
        private Directory workingDirectory = root;
        private final Map<Directory, Directory> parentMap = new HashMap<>();
    }

    static abstract class File {
        abstract int size();
    }

    static class Directory extends File {
        private final String name;
        private final Map<String, File> files;

        public Directory(String name, Map<String, File> files) {
            this.name = name;
            this.files = files;
        }

        int size() {
            int result = 0;
            for(final var file : files.values()) {
                result += file.size();
            }
            return result;
        }

        public String toString() {
            return "Directory{" +
                    "name='" + name + '\'' +
                    '}';
        }

        final Set<Directory> findDirectoriesSmallerThan(final int maxSize) {
            final var result = new HashSet<Directory>();
            for(final var file : files.values()) {
                try {
                    final var directory = (Directory)file;
                    result.addAll(directory.findDirectoriesSmallerThan(maxSize));
                } catch(final ClassCastException ignored) {
                }
            }
            if(size() < maxSize) { // FIXME duplicated traversal
                result.add(this);
            }
            return Collections.unmodifiableSet(result);
       }
       final Set<Directory> findDirectoriesLargerThan(final int minSize) {
            final var result = new HashSet<Directory>();
            if(size() >= minSize) {
                for (final var file : files.values()) { // FIXME duplicated traversal
                    try {
                        final var directory = (Directory) file;
                        result.addAll(directory.findDirectoriesLargerThan(minSize));
                    } catch (final ClassCastException ignored) {
                    }
                }
                result.add(this);
            }
            return Collections.unmodifiableSet(result);
       }
    }

    static class Leaf extends File {
        private final String name;
        private final int size;

        public Leaf(String name, int size) {
            this.name = name;
            this.size = size;
        }

        int size() {
            return size;
        }

        @Override
        public String toString() {
            return "Leaf{" +
                    "name='" + name + '\'' +
                    ", size=" + size +
                    '}';
        }
    }

    static abstract class Line {
        static Line parse(final String line) {
            if(line.startsWith("$")) {
                return Command.parse(line);
            }
            return Output.parse(line);
        }
        abstract void execute(Session session);
    }

    static abstract class Command extends Line {
        static Command parse(final String line) {
            if(line.startsWith("$ cd")) {
                return ChangeDirectory.parse(line);
            }
            return ListContents.parse(line);
        }
    }

    static class ListContents extends Command {
        void execute(final Session session) {
        }
        static Command parse(final String ignored) {
            return new ListContents();
        }
    }

    static class ChangeDirectory extends Command {
        private final String argument;

        public ChangeDirectory(String argument) {
            this.argument = argument;
        }

        void execute(Session session) {
            if("..".equals(argument)) {
                final var parent = session.parentMap.get(session.workingDirectory);
                if(parent == null) {
                    throw new IllegalArgumentException("Working directory has no parent: " + session.workingDirectory);
                }
                session.workingDirectory = parent;
            } else if( "/".equals(argument)) {
                session.workingDirectory = session.root;
            } else {
                final var target = (Directory) session.workingDirectory.files.get(argument);
                if(target == null) {
                    throw new IllegalArgumentException("No directory named \"" + argument + "\" inside \"" + session.workingDirectory + "\"");
                }
                session.workingDirectory = target;
            }
        }

        static ChangeDirectory parse(final String line) {
            return new ChangeDirectory(line.split(" ")[2]);
        }
    }

    static abstract class Output extends Line {
        static Output parse(final String line) {
            if(line.startsWith("dir")) {
                return DirectoryListing.parse(line);
            }
            return LeafListing.parse(line);
        }
    }

    static class DirectoryListing extends Output {
        private final String name;

        public DirectoryListing(String name) {
            this.name = name;
        }

        void execute(Session session) {
            final var directory = new Directory(name, new HashMap<>());
            session.parentMap.put(directory, session.workingDirectory); // TODO method on Session
            session.workingDirectory.files.put(name, directory);
        }

        static DirectoryListing parse(final String line) {
            final var components = line.split(" ");
            return new DirectoryListing(components[1]);
        }
    }

    static class LeafListing extends Output {
        private final int size;
        private final String name;

        public LeafListing(int size, String name) {
            this.size = size;
            this.name = name;
        }

        void execute(Session session) {
            session.workingDirectory.files.put(name, new Leaf(name, size));
        }

        static LeafListing parse(final String line) {
            final var components = line.split(" ");
            final var size = Integer.parseInt(components[0]);
            final var name = components[1];
            return new LeafListing(size, name);
        }
    }

    protected Stream<Line> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-07.txt"),
                        false)
                .map(Line::parse);
    }

    @Test
    public final void part1() {
        final var session = new Session();
        getInput().forEach(line -> line.execute(session));
        final var result = session.root.findDirectoriesSmallerThan(100_000).stream().mapToInt(Directory::size).sum();
        System.out.println("Part 1: " + result);
    }

    @Test
    public final void part2() {
        final var session = new Session();
        getInput().forEach(line -> line.execute(session));
        final var consumed = session.root.size();
        final var unused = 70_000_000 - consumed;
        final var required = 30_000_000 - unused;
        final var result = session.root.findDirectoriesLargerThan(required)
                .stream()
                .min(Comparator.comparing(Directory::size))
                .get()
                .size();
        System.out.println("Part 2: " + result);
    }

}
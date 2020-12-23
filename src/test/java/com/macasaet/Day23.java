package com.macasaet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

public class Day23 {

    public static void main(final String[] args) throws IOException {
        final var labels = new ArrayList<Integer>();
        try (var inputStream = Day23.class.getResourceAsStream("/day-23-input.txt")) {
            for (final var c : new String(inputStream.readAllBytes()).strip().toCharArray()) {
                labels.add(Integer.parseInt("" + c));
            }
        }

        final var map = new Cup[1_000_0000 + 1];
        Cup first = null;
        Cup current = null;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (final int label : labels) {
            final var cup = new Cup(label);
            map[label] = cup;
            if (first == null) {
                first = cup;
            } else {
                current.next = cup;
            }
            current = cup;

            min = Math.min(min, label);
            max = Math.max(max, label);
        }

        for (int i = max + 1; i <= 1_000_000; i++) { // Part 2
            final var cup = new Cup(i);
            map[i] = cup;
            current.next = cup;
            current = cup;
        }
        max = 1_000_000;
        current.next = first;
        current = current.next;

        for (int moveId = 1; moveId <= 10_000_000; moveId++) { // Part 1 looped only 100 times
            final var a = current.take();
            final var b = current.take();
            final var c = current.take();

            int destinationValue = current.label - 1;
            while (destinationValue == a.label || destinationValue == b.label || destinationValue == c.label || destinationValue < min || destinationValue > max) {
                destinationValue--;
                if (destinationValue < min) {
                    destinationValue = max;
                }
            }

            final var destination = map[destinationValue];
            c.linkBefore(destination.next);
            b.linkBefore(c);
            a.linkBefore(b);
            destination.linkBefore(a);

            current = current.next;
        }
        final var reference = map[1];
//        String result = "";
//        var cursor = reference.next;
//        for (int i = labels.size() - 1; --i >= 0; ) {
//            result += cursor.label;
//            cursor = cursor.next;
//        }
//        System.out.println("Part 1: " + result);
        final BigInteger x = new BigInteger("" + map[reference.next.label].label);
        final BigInteger y = new BigInteger("" + map[reference.next.next.label].label);
        System.out.println("Part 2: " + x.multiply(y).toString());
    }

    protected static class Cup {
        private final int label;
        private Cup next;

        public Cup(final int label) {
            this.label = label;
        }

        public Cup take() {
            final Cup retval = next;
            next = retval.next;
            retval.next = null;
            return retval;
        }

        public void linkBefore(final Cup cup) {
            next = cup;
        }

        public String toString() {
            return "Cup{ label=" + label + ", next=" + (next != null ? next.label : null) + " }";
        }
    }

}
package com.macasaet;

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Day18 {

    public static void main(final String[] args) throws IOException {
        try (var spliterator = new LineSpliterator(Day18.class.getResourceAsStream("/day-18-input.txt"))) {
            final var lines = StreamSupport.stream(spliterator, false).collect(Collectors.toUnmodifiableList());
            BigInteger sum = BigInteger.ZERO;
            for (final var line : lines) {
                final var outputQueue = new LinkedList<String>();
                final var operatorStack = new LinkedList<Character>();

                String numberString = "";
                for (final char c : line.toCharArray()) {
                    if (Character.isDigit(c)) {
                        numberString += c;
                    } else {
                        if (!"".equals(numberString)) {
                            outputQueue.add(numberString);
                            numberString = "";
                        }
                        switch (c) {
                            case '+', '*' -> {
                                Character topOperator = operatorStack.peek();
                                while (topOperator != null && topOperator > c /*&& topOperator != '('*/) {
                                    outputQueue.add("" + operatorStack.pop());
                                    topOperator = operatorStack.peek();
                                }
                                operatorStack.push(c);
                            }
                            case '(' -> operatorStack.push(c);
                            case ')' -> {
                                Character topOperator = operatorStack.peek();
                                while (topOperator != null && topOperator != '(') {
                                    outputQueue.add("" + operatorStack.pop());
                                    topOperator = operatorStack.peek();
                                }
                                if (topOperator == null /*|| topOperator != '('*/) {
                                    throw new IllegalStateException("mis-matched parentheses :-(");
                                }
                                operatorStack.pop();
                            }
                            case ' ' -> {
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + c);
                        }
                    }

                }
                if (!numberString.isBlank()) {
                    outputQueue.add(numberString);
                }
                while (!operatorStack.isEmpty()) {
                    outputQueue.add("" + operatorStack.pop());
                }
                final var stack = new LinkedList<BigInteger>();
                for (final var item : outputQueue) {
                    try {
                        stack.push(new BigInteger(item));
                    } catch (final NumberFormatException nfe) {
                        switch (item) {
                            case "+" -> {
                                final var rValue = stack.pop();
                                final var lValue = stack.pop();
                                final var result = lValue.add(rValue);
                                stack.push(result);
                            }
                            case "*" -> {
                                final var rValue = stack.pop();
                                final var lValue = stack.pop();
                                final var result = lValue.multiply(rValue);
                                stack.push(result);
                            }
                        }
                    }
                }
                if (stack.size() != 1) {
                    throw new IllegalStateException("Invalid stack: " + stack);
                }
                sum = sum.add(stack.get(0));
            }
            System.out.println("Part 2: " + sum);
        }
    }

}
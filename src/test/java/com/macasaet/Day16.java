package com.macasaet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * --- Day 16: Packet Decoder ---
 */
public class Day16 {

    protected Stream<String> getInput() {
        return StreamSupport
                .stream(new LineSpliterator("day-16.txt"),
                        false);
    }

    public interface Packet {
        long version();

        void accept(PacketVisitor packetVisitor);

        long evaluate();
    }

    public record Literal(long version, long value) implements Packet {

        public void accept(PacketVisitor packetVisitor) {
            packetVisitor.visit(this);
        }

        public long evaluate() {
            return value();
        }
    }

    public enum OperatorType {
        SUM {
            public long evaluate(List<? extends Packet> operands) {
                return operands.stream().mapToLong(Packet::evaluate).sum();
            }
        },
        PRODUCT {
            public long evaluate(List<? extends Packet> operands) {
                return operands.stream().mapToLong(Packet::evaluate).reduce(1, (x, y) -> x * y);
            }
        },
        MINIMUM {
            public long evaluate(List<? extends Packet> operands) {
                return operands.stream().mapToLong(Packet::evaluate).min().orElseThrow();
            }
        },
        MAXIMUM {
            public long evaluate(List<? extends Packet> operands) {
                return operands.stream().mapToLong(Packet::evaluate).max().orElseThrow();
            }
        },
        GREATER_THAN {
            public long evaluate(List<? extends Packet> operands) {
                if (operands.size() != 2) {
                    throw new IllegalArgumentException("Invalid operand list for \"greater than\" operator: " + operands);
                }
                final var x = operands.get(0).evaluate();
                final var y = operands.get(1).evaluate();
                return x > y ? 1 : 0;
            }
        },
        LESS_THAN {
            public long evaluate(List<? extends Packet> operands) {
                if (operands.size() != 2) {
                    throw new IllegalStateException("Invalid operand list for \"less than\" operator: " + operands);
                }
                final var x = operands.get(0).evaluate();
                final var y = operands.get(1).evaluate();
                return x < y ? 1 : 0;
            }
        },
        EQUAL_TO {
            public long evaluate(List<? extends Packet> operands) {
                if (operands.size() != 2) {
                    throw new IllegalStateException("Invalid operand list for \"equal to\" operator: " + operands);
                }
                final var x = operands.get(0).evaluate();
                final var y = operands.get(1).evaluate();
                return x == y ? 1 : 0;
            }
        };

        public abstract long evaluate(List<? extends Packet> operands);

        public static OperatorType forId(final int typeId) {
            return switch (typeId) {
                case 0 -> SUM;
                case 1 -> PRODUCT;
                case 2 -> MINIMUM;
                case 3 -> MAXIMUM;
                case 5 -> GREATER_THAN;
                case 6 -> LESS_THAN;
                case 7 -> EQUAL_TO;
                default -> throw new IllegalArgumentException("Invalid operator type ID: " + typeId);
            };
        }
    }

    public record Operator(long version, OperatorType operatorType, List<Packet> operands) implements Packet {

        public void accept(PacketVisitor packetVisitor) {
            packetVisitor.enter(this);
            for (final var subPacket : operands()) {
                subPacket.accept(packetVisitor);
            }
            packetVisitor.exit(this);
        }

        public long evaluate() {
            return operatorType().evaluate(operands());
        }
    }

    public interface PacketVisitor {
        void visit(Literal literal);

        void enter(Operator operator);

        void exit(Operator operator);
    }

    public static class PacketBuilder {

        private long version;
        private long typeId;
        private OptionalLong literalValue = OptionalLong.empty();
        private final List<Packet> subPackets = new ArrayList<>();

        public Packet readHex(final String hexString) {
            final var hexDigits = hexString.toCharArray();
            final var bits = hexToBits(hexDigits);
            read(bits, 0);
            return toPacket();
        }

        public int read(final List<Byte> bits, int transmissionCursor) {
            final var versionBits = bits.subList(transmissionCursor, transmissionCursor + 3);
            transmissionCursor += 3;
            this.version = toLong(versionBits);

            final var typeBits = bits.subList(transmissionCursor, transmissionCursor + 3);
            transmissionCursor += 3;
            this.typeId = toLong(typeBits);

            // TODO consider adding methods to parse each type specifically
            if (this.typeId == 4) {
                boolean finalGroup = false;
                final var literalBits = new ArrayList<Byte>();
                while (!finalGroup) {
                    final var groupBits = bits.subList(transmissionCursor, transmissionCursor + 5);
                    transmissionCursor += 5;
                    finalGroup = groupBits.get(0) == 0;
                    literalBits.addAll(groupBits.subList(1, 5));
                }
                if (literalBits.size() > 63) {
                    throw new IllegalArgumentException("Literal is too large for an long: " + literalBits.size());
                }
                literalValue = OptionalLong.of(toLong(literalBits));
                return transmissionCursor;
            } else {
                final var lengthTypeIdBits = bits.subList(transmissionCursor, transmissionCursor + 1);
                transmissionCursor += 1;
                final var lengthTypeId = toLong(lengthTypeIdBits);
                if (lengthTypeId == 0) {
                    final var lengthOfSubPacketsBits = bits.subList(transmissionCursor, transmissionCursor + 15);
                    transmissionCursor += 15;
                    final var lengthOfSubPackets = toLong(lengthOfSubPacketsBits);
                    int bitsRead = 0;
                    while (bitsRead < lengthOfSubPackets) {
                        final var subPacketBuilder = new PacketBuilder();
                        final var newCursor = subPacketBuilder.read(bits, transmissionCursor);
                        final var subPacketSize = newCursor - transmissionCursor; // size of sub-packet in bits
                        transmissionCursor = newCursor;

                        subPackets.add(subPacketBuilder.toPacket());
                        bitsRead += subPacketSize;
                    }
                    return transmissionCursor;
                } else if (lengthTypeId == 1) {
                    final var numSubPacketsBits = bits.subList(transmissionCursor, transmissionCursor + 11);
                    transmissionCursor += 11;
                    final var numSubPackets = toLong(numSubPacketsBits);
                    for (int packetsRead = 0; packetsRead < numSubPackets; packetsRead++) {
                        final var subPacketBuilder = new PacketBuilder();
                        transmissionCursor = subPacketBuilder.read(bits, transmissionCursor);
                        subPackets.add(subPacketBuilder.toPacket());
                    }
                    return transmissionCursor;
                } else {
                    throw new IllegalArgumentException("Invalid length type ID: " + lengthTypeId);
                }
            }
        }

        public Packet toPacket() {
            if (typeId == 4) {
                return new Literal(version, literalValue.orElseThrow());
            } else {
                return new Operator(version, OperatorType.forId((int) typeId), subPackets);
            }
        }

        protected long toLong(final List<Byte> bits) {
            long result = 0;
            for (int i = 0; i < bits.size(); i++) {
                final var bit = bits.get(i);
                if (bit == 1) {
                    final long shiftDistance = bits.size() - i - 1;
                    result |= 1L << shiftDistance;
                } else if (bit != 0) {
                    throw new IllegalArgumentException("Invalid bit representation of an integer: " + bits);
                }
            }
            return result;
        }

        protected List<Byte> hexToBits(final char[] hexDigits) {
            final var result = new ArrayList<Byte>(hexDigits.length * 4);
            for (final var digit : hexDigits) {
                final var bits = switch (digit) {
                    case '0' -> Arrays.asList((byte) 0, (byte) 0, (byte) 0, (byte) 0);
                    case '1' -> Arrays.asList((byte) 0, (byte) 0, (byte) 0, (byte) 1);
                    case '2' -> Arrays.asList((byte) 0, (byte) 0, (byte) 1, (byte) 0);
                    case '3' -> Arrays.asList((byte) 0, (byte) 0, (byte) 1, (byte) 1);
                    case '4' -> Arrays.asList((byte) 0, (byte) 1, (byte) 0, (byte) 0);
                    case '5' -> Arrays.asList((byte) 0, (byte) 1, (byte) 0, (byte) 1);
                    case '6' -> Arrays.asList((byte) 0, (byte) 1, (byte) 1, (byte) 0);
                    case '7' -> Arrays.asList((byte) 0, (byte) 1, (byte) 1, (byte) 1);
                    case '8' -> Arrays.asList((byte) 1, (byte) 0, (byte) 0, (byte) 0);
                    case '9' -> Arrays.asList((byte) 1, (byte) 0, (byte) 0, (byte) 1);
                    case 'A', 'a' -> Arrays.asList((byte) 1, (byte) 0, (byte) 1, (byte) 0);
                    case 'B', 'b' -> Arrays.asList((byte) 1, (byte) 0, (byte) 1, (byte) 1);
                    case 'C', 'c' -> Arrays.asList((byte) 1, (byte) 1, (byte) 0, (byte) 0);
                    case 'D', 'd' -> Arrays.asList((byte) 1, (byte) 1, (byte) 0, (byte) 1);
                    case 'E', 'e' -> Arrays.asList((byte) 1, (byte) 1, (byte) 1, (byte) 0);
                    case 'F', 'f' -> Arrays.asList((byte) 1, (byte) 1, (byte) 1, (byte) 1);
                    default -> throw new IllegalStateException("Unexpected value: " + digit);
                };
                result.addAll(bits);
            }
            return Collections.unmodifiableList(result);
        }
    }

    @Test
    public final void testParseLiteral() {
        // given
        final var input = "D2FE28";
        final var builder = new PacketBuilder();

        // when
        final var result = builder.readHex(input);

        // then
        assertTrue(result instanceof Literal);
        final var literal = (Literal) result;
        assertEquals(2021, literal.value);
    }

    @Test
    public final void testOperatorWithTwoSubPackets() {
        // given
        final var input = "38006F45291200";
        final var builder = new PacketBuilder();

        // when
        final var result = builder.readHex(input);

        // then
        assertTrue(result instanceof Operator);
        final var operator = (Operator) result;
        assertEquals(1, operator.version());
        assertEquals(OperatorType.LESS_THAN, operator.operatorType());
        assertEquals(2, operator.operands().size());
        final var a = (Literal) operator.operands().get(0);
        assertEquals(10, a.value());
        final var b = (Literal) operator.operands().get(1);
        assertEquals(20, b.value());
    }

    @Test
    public final void part1() {
        final var line = getInput().collect(Collectors.toList()).get(0);
        final var builder = new PacketBuilder();
        final var packet = builder.readHex(line);
        class VersionSummer implements PacketVisitor {

            int sum = 0;

            public void visit(Literal literal) {
                sum += literal.version();
            }

            public void enter(Operator operator) {
            }

            public void exit(Operator operator) {
                sum += operator.version();
            }
        }
        final var summer = new VersionSummer();
        packet.accept(summer);

        System.out.println("Part 1: " + summer.sum);
    }

    @Test
    public final void part2() {
        final var line = getInput().collect(Collectors.toList()).get(0);
        final var builder = new PacketBuilder();
        final var packet = builder.readHex(line);
        System.out.println("Part 2: " + packet.evaluate());
    }

    @Nested
    public class PacketBuilderTest {
        @Test
        public void testToInt() {
            // given
            final var builder = new PacketBuilder();
            // when
            // then
            assertEquals(2021, builder.toLong(Arrays.asList((byte) 0, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 1, (byte) 0, (byte) 0, (byte) 1, (byte) 0, (byte) 1)));
        }

        @Test
        public final void testMaths() {
            assertEquals(3, new PacketBuilder().readHex("C200B40A82").evaluate());
            assertEquals(54, new PacketBuilder().readHex("04005AC33890").evaluate());
            assertEquals(7, new PacketBuilder().readHex("880086C3E88112").evaluate());
            assertEquals(9, new PacketBuilder().readHex("CE00C43D881120").evaluate());
            assertEquals(1, new PacketBuilder().readHex("D8005AC2A8F0").evaluate());
            assertEquals(0, new PacketBuilder().readHex("F600BC2D8F").evaluate());
            assertEquals(0, new PacketBuilder().readHex("9C005AC2F8F0").evaluate());
            assertEquals(1, new PacketBuilder().readHex("9C0141080250320F1802104A08").evaluate());
        }
    }

}
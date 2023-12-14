package org.generator;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Formatter;

public record UniqueId(byte[] id) implements Comparable<UniqueId> {

    public static UniqueId fromString(String uuidString) {
        if (uuidString == null || uuidString.length() != 36) {
            throw new IllegalArgumentException("UUID string must be exactly 36 characters long");
        }

        String hexString = uuidString.replaceAll("-", "");

        if (hexString.length() != 32) {
            throw new IllegalArgumentException("Invalid UUID string format");
        }

        byte[] bytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            int index = i * 2;
            int value = Integer.parseInt(hexString.substring(index, index + 2), 16);
            bytes[i] = (byte) value;
        }
        return new UniqueId(bytes);
    }

    public static UniqueId from(long timestamp, short sequence, byte[] nodeId) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(timestamp);
        buffer.putShort(sequence);
        buffer.put(nodeId);
        byte[] id = buffer.array();
        if (id.length != 16) {
            throw new IllegalArgumentException("Byte array must be exactly 16 bytes long");
        }
        return new UniqueId(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniqueId uniqueId = (UniqueId) o;
        return Arrays.equals(id, uniqueId.id);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(id);
    }

    @Override
    public int compareTo(UniqueId other) {
        return Arrays.compare(id, other.id);
    }

    @Override
    public String toString() {
        try (Formatter formatter = new Formatter()) {
            for (int i = 0; i < id.length; i++) {
                // Add hyphens in the appropriate positions
                if (i == 4 || i == 6 || i == 8 || i == 10) {
                    formatter.format("-");
                }
                // Format each byte as a two-digit hexadecimal number
                formatter.format("%02x", id[i]);
            }

            return formatter.toString();
        }
    }

    public long timestamp() {
        return ByteBuffer.wrap(Arrays.copyOfRange(id, 0, 8)).getLong();
    }

    public short sequence() {
        return ByteBuffer.wrap(Arrays.copyOfRange(id, 8, 10)).getShort();
    }

    public byte[] nodeId() {
        return Arrays.copyOfRange(id, 10, 16);
    }
}

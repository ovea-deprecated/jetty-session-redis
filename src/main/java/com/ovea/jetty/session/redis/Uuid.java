package com.ovea.jetty.session.redis;

import java.io.Serializable;
import java.util.UUID;

final class Uuid implements Serializable, Comparable<Uuid> {

    private static final long serialVersionUID = 1;

    private final UUID internal;

    private Uuid(UUID uuid) {
        this.internal = uuid;
    }

    public static Uuid from(String uuidString) {
        if (uuidString == null)
            throw new IllegalArgumentException("Illegal UUID string");
        byte[] b = Base64.decode(uuidString);
        // the string is the B64 representation of 128bits: two long
        if (b.length != 16)
            throw new IllegalArgumentException("Invalid UUID string");
        return new Uuid(fromBytes(b));
    }

    public static Uuid generate() {
        return new Uuid(UUID.randomUUID());
    }

    @Override
    public int compareTo(Uuid o) {
        return internal.compareTo(o.internal);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Uuid uuid = (Uuid) o;
        return internal.equals(uuid.internal);
    }

    @Override
    public int hashCode() {
        return internal.hashCode();
    }

    @Override
    public String toString() {
        return Base64.encodeToString(toBytes(internal));
    }

    private static UUID fromBytes(byte[] data) {
        long msb = 0;
        long lsb = 0;
        for (int i = 7; i >= 0; i--)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i = 15; i >= 8; i--)
            lsb = (lsb << 8) | (data[i] & 0xff);
        return new UUID(msb, lsb);
    }

    private static byte[] toBytes(UUID uuid) {
        byte[] data = new byte[16];
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            data[i] = (byte) (msb & 0xff);
            msb >>>= 8;
        }
        for (int i = 8; i < 16; i++) {
            data[i] = (byte) (lsb & 0xff);
            lsb >>>= 8;
        }
        return data;
    }

    public UUID internal() {
        return internal;
    }
}

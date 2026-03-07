package net.dehasher.hlib.file.util;

public final class Validate {
    public static <T> T notNull(final T obj) {
        if (obj == null) throw new IllegalArgumentException();
        return obj;
    }

    public static <T> void notNull(final T obj, final String msgIfNull) {
        if (obj == null) throw new IllegalArgumentException(msgIfNull);
    }

    public static String notEmpty(final String obj) {
        if (obj == null || obj.trim().isEmpty()) throw new IllegalArgumentException();
        return obj;
    }

    public static void notEmpty(final String obj, final String msgIfNull) {
        if (obj == null || obj.trim().isEmpty()) throw new IllegalArgumentException(msgIfNull);
    }


    public static void arrayBounds(final int off, final int len, final int arrayLength, final String msgPrefix) {
        if (off < 0 || len < 0 || (arrayLength - off) < len) throw new ArrayIndexOutOfBoundsException(msgPrefix + ": off: " + off + ", len: " + len + ", array length: " + arrayLength);
    }

    public static void isTrue(boolean bool, String msgIfFalse) {
        if (!bool) throw new IllegalArgumentException(msgIfFalse);
    }

    private Validate() {
        throw new RuntimeException();
    }
}
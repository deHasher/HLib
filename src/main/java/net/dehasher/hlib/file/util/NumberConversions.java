package net.dehasher.hlib.file.util;

import net.dehasher.hlib.Tools;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NumberConversions {
    private NumberConversions() {}

    public static int floor(double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int ceil(final double num) {
        final int floor = (int) num;
        return floor == num ? floor : floor + (int) (~Double.doubleToRawLongBits(num) >>> 63);
    }

    public static int round(double num) {
        return floor(num + 0.5d);
    }

    public static double square(double num) {
        return num * num;
    }

    @SuppressWarnings("DataFlowIssue")
    public static int toInt(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).intValue();
        }

        try {
            return Tools.parseInt(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public static float toFloat(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).floatValue();
        }

        try {
            return Tools.parseFloat(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public static double toDouble(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).doubleValue();
        }

        try {
            return Tools.parseDouble(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public static long toLong(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).longValue();
        }

        try {
            return Tools.parseLong(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public static short toShort(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).shortValue();
        }

        try {
            return Tools.parseShort(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    @SuppressWarnings("DataFlowIssue")
    public static byte toByte(@Nullable Object object) {
        if (object instanceof Number) {
            return ((Number) object).byteValue();
        }

        try {
            return Tools.parseByte(object.toString());
        } catch (NumberFormatException | NullPointerException ignored) {}
        return 0;
    }

    public static boolean isFinite(double d) {
        return Math.abs(d) <= Double.MAX_VALUE;
    }

    public static boolean isFinite(float f) {
        return Math.abs(f) <= Float.MAX_VALUE;
    }

    public static void checkFinite(double d, @NotNull String message) {
        if (!isFinite(d)) throw new IllegalArgumentException(message);
    }

    public static void checkFinite(float d, @NotNull String message) {
        if (!isFinite(d)) throw new IllegalArgumentException(message);
    }
}
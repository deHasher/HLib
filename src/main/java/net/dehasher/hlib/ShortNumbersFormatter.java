package net.dehasher.hlib;

import net.dehasher.hlib.config.Info;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ShortNumbersFormatter {
    private record Suffix(String name, long multiplier) {}
    private static volatile List<Suffix> suffixes = List.of();

    public static String format(int number) {
        return format((long) number);
    }

    public static String format(long number) {
        return getSuffixes().stream()
                .filter(suffix -> number >= suffix.multiplier() || number <= -suffix.multiplier())
                .findFirst()
                .map(suffix -> {
                    long value = number / suffix.multiplier();
                    if (suffix.name().isEmpty()) return String.valueOf(value);
                    return value + " " + suffix.name();
                })
                .orElse(String.valueOf(number));
    }

    public static void reload() {
        suffixes = parseSuffixes(Info.shortNumbersSuffixes);
    }

    private static List<Suffix> getSuffixes() {
        if (suffixes.isEmpty()) reload();
        return suffixes;
    }

    private static List<Suffix> parseSuffixes(List<String> rawSuffixes) {
        List<Suffix> result = new ArrayList<>();

        if (rawSuffixes == null || rawSuffixes.isEmpty()) {
            result.add(new Suffix("", 1));
            return List.copyOf(result);
        }

        rawSuffixes.forEach(rawSuffix -> {
            if (rawSuffix == null || rawSuffix.isBlank()) {
                result.add(new Suffix("", 1));
                return;
            }

            String[] parts = rawSuffix.split("-", 2);
            if (parts.length != 2) return;

            String name = parts[0].trim();
            long multiplier = Long.parseLong(parts[1].trim());

            if (multiplier <= 0) return;
            result.add(new Suffix(name, multiplier));
        });

        result.sort(Comparator.comparingLong(Suffix::multiplier).reversed());

        return List.copyOf(result);
    }
}
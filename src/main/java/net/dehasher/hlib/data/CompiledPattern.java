package net.dehasher.hlib.data;

import lombok.Getter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CompiledPattern {
    COLOR(Pattern.compile("(?i)" + '§' + "[0-9A-FK-ORX]")),
    UUID(Pattern.compile("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)")),
    BRACKET_HEX_FORMAT(Pattern.compile("\\{#[0-9a-fA-F]{6}}")),
    AMPERSAND_HEX_FORMAT(Pattern.compile("&#[0-9a-fA-F]{6}")),
    MOJANG_HEX_FORMAT(Pattern.compile("&x[&0-9a-fA-F]{12}")),
    GRADIENT_HEX_FORMAT(Pattern.compile("\\{#[0-9a-fA-F]{6}>}[^{]*\\{#[0-9a-fA-F]{6}<}")),
    PLAYER(Pattern.compile("^[a-zA-Z0-9_]{3,16}$")),
    FAWE(Pattern.compile("[a-zA-Z0-9_ /:,%]+")),
    LOG4J(Pattern.compile(".*\\$\\{[^}]*}.*")),
    CYRILLIC(Pattern.compile(".*\\p{InCyrillic}.*")),
    IP(Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$")),
    ADVERTISEMENT(Pattern.compile("[-a-zA-Zа-яА-Я0-9@:%_+.~#?&/=]{2,256}\\.[a-zA-Z]{2,4}\\b(/[-a-zA-Zа-яА-Я0-9@:%_+~#?&/=]*)?")),
    YOUTUBE(Pattern.compile("https?://(?:m.)?(?:www\\.)?youtu(?:\\.be/|(?:be-nocookie|be)\\.com/(?:watch|\\w+\\?(?:feature=\\w+.\\w+&)?v=|v/|e/|embed/|shorts/|user/(?:[\\w#]+/)+))([^&#?\\n]+)", Pattern.CASE_INSENSITIVE)),
    RUTUBE(Pattern.compile("rutube\\.ru/(video|shorts)/[a-zA-Z0-9]{32}", Pattern.CASE_INSENSITIVE)),
    AI_BOLD(Pattern.compile("\\*\\*(.*?)\\*\\*")),
    IMAGE_URL(Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")),
    ONLINE(Pattern.compile("\\{online_(.*?)}")),
    IMAGE_NAME(Pattern.compile("[^a-zA-Z0-9а-яА-ЯёЁ\\-_,.]")),
    CUSTOM_TIME_FORMAT(Pattern.compile("^\\d+[smhdwy]$"));

    @Getter
    private final Pattern value;

    CompiledPattern(Pattern pattern) {
        this.value = pattern;
    }

    // Вернёт true, если есть хоть 1 совпадение с паттерном.
    public boolean find(String input) {
        return this.value.matcher(input).find();
    }

    // Вернёт true, если вся строка совпадает с паттерном.
    public boolean matches(String input) {
        return matcher(input).matches();
    }

    public Matcher matcher(String input) {
        return this.value.matcher(input);
    }

    public static boolean contains(List<String> patterns, String string) {
        return patterns.stream().anyMatch(pattern -> string.toLowerCase().matches(convertToRegex(pattern.toLowerCase())));
    }

    private static String convertToRegex(String pattern) {
        return pattern
                .replace(".", "\\.")
                .replace("*", "\\*")
                .replace("?", "\\?")
                .replace("%", ".*");
    }
}
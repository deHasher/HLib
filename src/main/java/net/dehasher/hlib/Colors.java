package net.dehasher.hlib;

import com.google.common.collect.Lists;
import com.google.common.primitives.Chars;
import lombok.AccessLevel;
import lombok.Getter;
import net.dehasher.hlib.data.CompiledPattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

public final class Colors {

    public static final char MAIN_COLOR_CHAR = '§';
    public static final char FAKE_COLOR_CHAR = '&';
    public static final String ALL_CODES     = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    @Getter
    private static final Map<String, Colors> allColors = new ConcurrentHashMap<>();
    @Getter(AccessLevel.PRIVATE)
    private static final Set<Colors> temp = ConcurrentHashMap.newKeySet();

    public static final Colors BLACK         = new Colors('0', "black", 0x000000);
    public static final Colors DARK_BLUE     = new Colors('1', "dark_blue", 0x0000AA);
    public static final Colors DARK_GREEN    = new Colors('2', "dark_green", 0x00AA00);
    public static final Colors DARK_AQUA     = new Colors('3', "dark_aqua", 0x00AAAA);
    public static final Colors DARK_RED      = new Colors('4', "dark_red", 0xAA0000);
    public static final Colors DARK_PURPLE   = new Colors('5', "dark_purple", 0xAA00AA);
    public static final Colors GOLD          = new Colors('6', "gold", 0xFFAA00);
    public static final Colors GRAY          = new Colors('7', "gray", 0xAAAAAA);
    public static final Colors DARK_GRAY     = new Colors('8', "dark_gray", 0x555555);
    public static final Colors BLUE          = new Colors('9', "blue", 0x5555FF);
    public static final Colors GREEN         = new Colors('a', "green", 0x55FF55);
    public static final Colors AQUA          = new Colors('b', "aqua", 0x55FFFF);
    public static final Colors RED           = new Colors('c', "red", 0xFF5555);
    public static final Colors LIGHT_PURPLE  = new Colors('d', "light_purple", 0xFF55FF);
    public static final Colors YELLOW        = new Colors('e', "yellow", 0xFFFF55);
    public static final Colors WHITE         = new Colors('f', "white", 0xFFFFFF);
    public static final Colors MAGIC         = new Colors('k', "obfuscated", null);
    public static final Colors BOLD          = new Colors('l', "bold", null);
    public static final Colors STRIKETHROUGH = new Colors('m', "strikethrough", null);
    public static final Colors UNDERLINE     = new Colors('n', "underline", null);
    public static final Colors ITALIC        = new Colors('o', "italic", null);
    public static final Colors RESET         = new Colors('r', "reset", null);

    private final String toString;

    @Getter
    private final Integer red;
    @Getter
    private final Integer green;
    @Getter
    private final Integer blue;

    private Colors(String name) {
        this(null, name, name);
    }

    private Colors(Character code, String name, int hex) {
        this(code, name, Integer.toHexString(hex));
    }

    private Colors(Character code, String name, String hex) {
        Color color   = hex   != null ? getCustomColor(hex) : null;
        this.red      = color != null ? color.getRed()      : null;
        this.green    = color != null ? color.getGreen()    : null;
        this.blue     = color != null ? color.getBlue()     : null;
        this.toString = code  != null ? new String(new char[]{ MAIN_COLOR_CHAR, code }) : generateMagic(name);
        getAllColors().put(name, this);
        if (name.contains("_")) getAllColors().put(name.replace("_", ""), this);
        if (code != null && hex != null) getAllColors().put(Character.toString(code), this);
    }

    @Override
    public boolean equals(Object value) {
        if (this == value) return true;
        if (value == null || getClass() != value.getClass()) return false;

        Colors other = (Colors) value;

        return Objects.equals(this.toString, other.toString);
    }

    @Override
    public String toString() {
        return toString;
    }

    // Удалить цвета в строке.
    public static String clear(Object object) {
        if (object instanceof Component component) return plain(component);
        if (object instanceof String string) return CompiledPattern.COLOR.matcher(set(string)).replaceAll("");
        return null;
    }

    // Выводим только текст из строки.
    public static String plain(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    // Сравниваем строки.
    public static boolean cleanEquals(String string1, String string2) {
        return Colors.clear(string1).equals(Colors.clear(string2));
    }

    // Сравниваем строки.
    public static boolean cleanEquals(Component string1, String string2) {
        return Colors.clear(string1).equals(Colors.clear(string2));
    }

    // Сравниваем строки.
    public static boolean cleanEquals(String string1, Component string2) {
        return Colors.clear(string1).equals(Colors.clear(string2));
    }

    // Сравниваем строки.
    public static boolean cleanEquals(Component string1, Component string2) {
        return Colors.clear(string1).equals(Colors.clear(string2));
    }

    // Сравниваем строки без учёта регистра.
    public static boolean cleanEqualsIgnoreCase(String string1, String string2) {
        return Colors.clear(string1).equalsIgnoreCase(Colors.clear(string2));
    }

    // Сравниваем строки без учёта регистра.
    public static boolean cleanEqualsIgnoreCase(Component string1, String string2) {
        return Colors.clear(string1).equalsIgnoreCase(Colors.clear(string2));
    }

    // Сравниваем строки без учёта регистра.
    public static boolean cleanEqualsIgnoreCase(String string1, Component string2) {
        return Colors.clear(string1).equalsIgnoreCase(Colors.clear(string2));
    }

    // Сравниваем строки без учёта регистра.
    public static boolean cleanEqualsIgnoreCase(Component string1, Component string2) {
        return Colors.clear(string1).equalsIgnoreCase(Colors.clear(string2));
    }

    // В случае ошибок возвращаем этот текст.
    public static String none() {
        return RED + "-" + Colors.RESET;
    }

    // Покрасить список строк.
    public static List<String> set(List<String> list) {
        ArrayList<String> result = Lists.newArrayList();
        list.forEach(row -> result.add(set(row)));
        return result;
    }

    // Покрасить список строк.
    public static List<Component> setComponent(List<String> list) {
        ArrayList<Component> result = Lists.newArrayList();
        list.forEach(row -> result.add(setComponent(row)));
        return result;
    }

    public static Component setComponent(String component) {
        return LegacyComponentSerializer.legacySection().deserialize(Colors.set(component));
    }

    // Покрасить строку.
    public static String set(String value) {
        char[] chars = value.toCharArray();

        // Перебор всех символов в строке.
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] != FAKE_COLOR_CHAR) continue;
            if (ALL_CODES.indexOf(chars[i + 1]) == -1) continue;

            chars[i] = Colors.MAIN_COLOR_CHAR;
            chars[i + 1] = Character.toLowerCase(chars[i + 1]);
        }

        String result = new String(chars);
        result = setGradientHex(result);
        result = setCustomHex(CompiledPattern.AMPERSAND_HEX_FORMAT, result);
        result = setCustomHex(CompiledPattern.BRACKET_HEX_FORMAT, result);
        result = setMojangHex(result);

        return result;
    }

    private static String setCustomHex(CompiledPattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        while (matcher.find()) {
            value = value.replace(matcher.group(), get(matcher.group()).toString());
        }
        return value;
    }

    private static String setMojangHex(String value) {
        Matcher matcher = CompiledPattern.MOJANG_HEX_FORMAT.matcher(value);
        while (matcher.find()) {
            char[] data = matcher.group().toCharArray();
            value = value.replace(matcher.group(), get(String.valueOf(new char[]{data[3], data[5], data[7], data[9], data[11], data[13]})).toString());
        }
        return value;
    }

    private static String setGradientHex(String value) {
        Matcher matcher = CompiledPattern.GRADIENT_HEX_FORMAT.matcher(value);
        while (matcher.find()) {
            String format  = matcher.group();
            Colors start   = Colors.get(format.substring(2, 8));
            Colors end     = Colors.get(format.substring(format.length() - 8, format.length() - 2));
            value = value.replace(format, asGradient(start, format.substring(10, format.length() - 10), end));
        }
        return value;
    }

    private static String asGradient(Colors start, String message, Colors end) {
        StringBuilder sb = new StringBuilder();

        Set<Colors> styles = getColorStyles(message);
        message = clear(message);
        char[] chars = message.toCharArray();
        int length = chars.length;

        for (int i = 0; i < length; i++) {
            int red   = (int) (start.getRed()   + (float) (end.getRed()   - start.getRed())   / (length - 1) * i);
            int green = (int) (start.getGreen() + (float) (end.getGreen() - start.getGreen()) / (length - 1) * i);
            int blue  = (int) (start.getBlue()  + (float) (end.getBlue()  - start.getBlue())  / (length - 1) * i);
            sb.append(String.format("{#%02X%02X%02X}", red, green, blue).toLowerCase());
            styles.forEach(sb::append);
            sb.append(chars[i]);
        }

        return sb.toString();
    }

    private static Set<Colors> getColorStyles(String message) {
        getTemp().clear();
        message = set(message);
        if (message.contains(MAGIC.toString())) getTemp().add(MAGIC);
        if (message.contains(STRIKETHROUGH.toString())) getTemp().add(STRIKETHROUGH);
        if (message.contains(UNDERLINE.toString())) getTemp().add(UNDERLINE);
        if (message.contains(ITALIC.toString())) getTemp().add(ITALIC);
        if (message.contains(BOLD.toString())) getTemp().add(BOLD);
        return getTemp();
    }

    public static Colors get(String value) {
        if (value == null) return Colors.WHITE;
        value = value.toLowerCase().replaceAll("[#&{}]", "");

        Colors result = getAllColors().get(value);
        if (result != null) return result;

        try {
            Integer.parseInt(value, 16);
        } catch (Throwable t) {
            return Colors.WHITE;
        }

        return new Colors(value);
    }

    public static String generateMagic(String value) {
        StringBuilder magic = new StringBuilder(MAIN_COLOR_CHAR + "x");
        Chars.asList(value.toCharArray()).forEach(c -> magic.append(MAIN_COLOR_CHAR).append(c));
        return magic.toString();
    }

    public static Color getCustomColor(String value) {
        try {
            return Color.decode("#" + value.replace("#", ""));
        } catch (Throwable ignored) {
            return null;
        }
    }
}
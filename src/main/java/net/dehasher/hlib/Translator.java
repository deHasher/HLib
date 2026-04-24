package net.dehasher.hlib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dehasher.hlib.data.BukkitVersion;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.platform.bukkit.HLib;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Translator {
    @Getter
    private static final Map<Character, Character> russianKeymap = createRussianKeymap();

    private static final Locale  DEFAULT_LOCALE    = Locale.US;
    private static final String  LANG_MANIFEST_URL = "${url_repos}/minecraft/version/%s/manifest_lang.json";
    private static final Path    LANG_DIRECTORY    = Platform.get().isProxy() ? null : HLib.getInstance().getDataFolder().toPath().resolve("lang");
    private static final Pattern FORMAT_PATTERN    = Pattern.compile("%(?:(\\d+)\\$)?(?:[-#+ 0,(<]*)?(?:\\d+)?(?:\\.\\d+)?([A-Za-z%])");

    public static synchronized void init() {
        Scheduler.doAsync(() -> {
            try {
                String version = Tools.getBukkitVersionAsPatch();

                List<LocaleFileInfo> localeFiles = syncLocaleFiles(version);
                Map<Locale, Map<String, String>> localeTranslations = loadAllLocaleTranslations(version, localeFiles);
                if (localeTranslations.isEmpty()) throw new IllegalStateException("Minecraft locales not loaded for version: " + version);

                VanillaTranslationRegistry registry = new VanillaTranslationRegistry();
                registry.setDefaultLocale(DEFAULT_LOCALE);
                localeTranslations.forEach(registry::registerLocale);

                registry.install();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static String toRussianKeymap(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character character = getRussianKeymap().get(input.charAt(i));
            builder.append(character != null ? character : input.charAt(i));
        }
        return builder.toString();
    }

    public static String getMaterialName(Material material, CommandSender sender) {
        if (sender instanceof Player player) return getMaterialName(material, resolveLocale(player));
        return getMaterialName(material);
    }

    public static String getMaterialName(Material material, Player player) {
        return getMaterialName(material, resolveLocale(player));
    }

    public static String getMaterialName(Material material) {
        return getMaterialName(material, DEFAULT_LOCALE);
    }

    public static String getMaterialName(Material material, Locale locale) {
        return getName(material, locale);
    }

    public static String getBlockName(Block block, CommandSender sender) {
        if (sender instanceof Player player) return getBlockName(block, resolveLocale(player));
        return getBlockName(block);
    }

    public static String getBlockName(Block block, Player player) {
        return getBlockName(block, resolveLocale(player));
    }

    public static String getBlockName(Block block) {
        return getBlockName(block, DEFAULT_LOCALE);
    }

    public static String getBlockName(Block block, Locale locale) {
        return getName(block, locale);
    }

    public static String getName(String translationKey, CommandSender sender) {
        if (sender instanceof Player player) return getName(translationKey, resolveLocale(player));
        return getName(translationKey);
    }

    public static String getName(String translationKey, Player player) {
        return getName(translationKey, resolveLocale(player));
    }

    public static String getName(String translationKey) {
        return getName(translationKey, DEFAULT_LOCALE);
    }

    public static String getName(Object object, Player player) {
        return getName(object, resolveLocale(player));
    }

    public static String getName(Object object) {
        return getName(object, DEFAULT_LOCALE);
    }

    public static String getName(Object object, Locale locale) {
        String translationKey = resolveTranslationKey(object);
        Locale targetLocale = normalizeLocale(locale);

        Component component = Component.translatable(translationKey);
        Component rendered = GlobalTranslator.render(component, targetLocale);
        return PlainTextComponentSerializer.plainText().serialize(rendered);
    }

    private static List<LocaleFileInfo> syncLocaleFiles(String minecraftVersion) throws IOException {
        JsonElement manifestElement = readJsonElement(buildLangManifestUrl(minecraftVersion));
        List<LocaleFileInfo> localeFiles = parseLocaleManifest(manifestElement);
        if (localeFiles.isEmpty()) throw new IllegalStateException("Locale manifest is empty for version: " + minecraftVersion);

        Path versionDirectory = resolveLangVersionDirectory(minecraftVersion);
        Files.createDirectories(versionDirectory);

        for (LocaleFileInfo localeFile : localeFiles) {
            Path file = resolveLocaleFile(minecraftVersion, localeFile.id());
            if (isLocaleFileReady(file)) continue;

            String content = readText(localeFile.url());
            if (content.isBlank()) throw new IllegalStateException("Empty locale content for locale: " + localeFile.id());

            Files.writeString(file, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        }

        return localeFiles;
    }

    private static Map<Locale, Map<String, String>> loadAllLocaleTranslations(String minecraftVersion, List<LocaleFileInfo> localeFiles) {
        Map<Locale, Map<String, String>> locales = new ConcurrentHashMap<>();

        for (LocaleFileInfo localeFile : localeFiles) {
            Path file = resolveLocaleFile(minecraftVersion, localeFile.id());
            if (!Files.isRegularFile(file)) continue;

            try {
                String json = Files.readString(file, StandardCharsets.UTF_8);
                if (json.isBlank()) continue;
                Map<String, String> translations = parseTranslations(json);
                if (!translations.isEmpty()) locales.put(toLocale(localeFile.id()), translations);
            } catch (Throwable ignored) {}
        }

        return locales;
    }

    private static List<LocaleFileInfo> parseLocaleManifest(JsonElement element) {
        List<LocaleFileInfo> localeFiles = new ArrayList<>();
        if (element == null || element.isJsonNull()) return localeFiles;

        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                LocaleFileInfo localeFile = parseLocaleFileInfo(entry);
                if (localeFile != null) localeFiles.add(localeFile);
            }
            return localeFiles;
        }

        if (!element.isJsonObject()) {
            throw new IllegalStateException("Unsupported locale manifest format");
        }

        JsonObject object = element.getAsJsonObject();

        if (object.has("files") && object.get("files").isJsonArray()) {
            for (JsonElement entry : object.getAsJsonArray("files")) {
                LocaleFileInfo localeFile = parseLocaleFileInfo(entry);
                if (localeFile != null) localeFiles.add(localeFile);
            }
            return localeFiles;
        }

        LocaleFileInfo singleFile = parseLocaleFileInfo(object);
        if (singleFile != null) {
            localeFiles.add(singleFile);
            return localeFiles;
        }

        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            LocaleFileInfo localeFile = parseLocaleFileInfo(entry.getValue());
            if (localeFile != null) localeFiles.add(localeFile);
        }

        return localeFiles;
    }

    private static LocaleFileInfo parseLocaleFileInfo(JsonElement element) {
        if (element == null || !element.isJsonObject()) return null;

        JsonObject object = element.getAsJsonObject();
        if (!object.has("id") || !object.has("url")) return null;

        String id = normalizeMinecraftLocaleId(object.get("id").getAsString());
        String url = object.get("url").getAsString();

        if (id.isBlank() || url.isBlank()) return null;
        return new LocaleFileInfo(id, url);
    }

    private static boolean isLocaleFileReady(Path file) {
        try {
            return Files.isRegularFile(file) && Files.size(file) > 0;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Path resolveLangVersionDirectory(String minecraftVersion) {
        return LANG_DIRECTORY.resolve(minecraftVersion);
    }

    private static Path resolveLocaleFile(String minecraftVersion, String localeId) {
        return resolveLangVersionDirectory(minecraftVersion).resolve(normalizeMinecraftLocaleId(localeId) + ".json");
    }

    private static String buildLangManifestUrl(String minecraftVersion) {
        return String.format(LANG_MANIFEST_URL, minecraftVersion);
    }

    private static Locale resolveLocale(Player player) {
        if (player == null) return DEFAULT_LOCALE;
        try {
            return normalizeLocale(player.locale());
        } catch (Throwable ignored) {
            return DEFAULT_LOCALE;
        }
    }

    private static Locale normalizeLocale(Locale locale) {
        return locale != null ? locale : DEFAULT_LOCALE;
    }

    private static String resolveTranslationKey(Object object) {
        if (object == null) throw new IllegalArgumentException("Object is null");
        if (object instanceof String translationKey) return translationKey;
        if (object instanceof Material material) return material.getTranslationKey();
        if (object instanceof Block block) return block.getTranslationKey();

        try {
            Method method = object.getClass().getMethod("getTranslationKey");
            Object value = method.invoke(object);
            if (value instanceof String translationKey && !translationKey.isBlank()) return translationKey;
        } catch (Throwable ignored) {}

        throw new IllegalArgumentException("Object translation key not found: " + object.getClass().getName());
    }

    private static String normalizeMinecraftLocaleId(String minecraftLocale) {
        return minecraftLocale.toLowerCase(Locale.ROOT).replace("-", "_").trim();
    }

    private static Locale toLocale(String minecraftLocale) {
        String normalized = normalizeMinecraftLocaleId(minecraftLocale);
        String[] parts = normalized.split("_", 3);

        if (parts.length == 1) return new Locale(parts[0]);
        if (parts.length == 2) return new Locale(parts[0], parts[1].toUpperCase(Locale.ROOT));
        return new Locale(parts[0], parts[1].toUpperCase(Locale.ROOT), parts[2]);
    }

    private static JsonElement readJsonElement(String url) {
        String response = readText(url);
        return Tools.getGSON().fromJson(response, JsonElement.class);
    }

    private static String readText(String url) {
        String response = Informer.url(url);
        if (response.isBlank()) throw new IllegalStateException("Empty response from URL: " + url);
        return response;
    }

    private static Map<String, String> parseTranslations(String json) {
        JsonObject object = Tools.getGSON().fromJson(json, JsonObject.class);
        Map<String, String> translations = new ConcurrentHashMap<>();
        if (object == null) return translations;

        object.entrySet().forEach(entry -> {
            JsonElement value = entry.getValue();
            if (value == null || !value.isJsonPrimitive()) return;
            translations.put(entry.getKey(), value.getAsString());
        });

        return translations;
    }

    private static Map<Character, Character> createRussianKeymap() {
        Map<Character, Character> keymap = new ConcurrentHashMap<>();

        keymap.put('q', 'й'); keymap.put('w', 'ц');
        keymap.put('e', 'у'); keymap.put('r', 'к');
        keymap.put('t', 'е'); keymap.put('y', 'н');
        keymap.put('u', 'г'); keymap.put('i', 'ш');
        keymap.put('o', 'щ'); keymap.put('p', 'з');
        keymap.put('a', 'ф'); keymap.put('s', 'ы');
        keymap.put('d', 'в'); keymap.put('f', 'а');
        keymap.put('g', 'п'); keymap.put('h', 'р');
        keymap.put('j', 'о'); keymap.put('k', 'л');
        keymap.put('l', 'д'); keymap.put('z', 'я');
        keymap.put('x', 'ч'); keymap.put('c', 'с');
        keymap.put('v', 'м'); keymap.put('b', 'и');
        keymap.put('n', 'т'); keymap.put('m', 'ь');

        keymap.put('Q', 'Й'); keymap.put('W', 'Ц');
        keymap.put('E', 'У'); keymap.put('R', 'К');
        keymap.put('T', 'Е'); keymap.put('Y', 'Н');
        keymap.put('U', 'Г'); keymap.put('I', 'Ш');
        keymap.put('O', 'Щ'); keymap.put('P', 'З');
        keymap.put('A', 'Ф'); keymap.put('S', 'Ы');
        keymap.put('D', 'В'); keymap.put('F', 'А');
        keymap.put('G', 'П'); keymap.put('H', 'Р');
        keymap.put('J', 'О'); keymap.put('K', 'Л');
        keymap.put('L', 'Д'); keymap.put('Z', 'Я');
        keymap.put('X', 'Ч'); keymap.put('C', 'С');
        keymap.put('V', 'М'); keymap.put('B', 'И');
        keymap.put('N', 'Т'); keymap.put('M', 'Ь');

        return keymap;
    }

    private record LocaleFileInfo(String id, String url) {}

    public static final class VanillaTranslationRegistry {
        private final TranslationRegistry registry = TranslationRegistry.create(Key.key("minecraft", "vanilla"));

        public void setDefaultLocale(Locale locale) {
            registry.defaultLocale(locale);
        }

        public void registerLocale(Locale locale, Map<String, String> translations) {
            Map<String, MessageFormat> formats = new ConcurrentHashMap<>();
            translations.forEach((key, value) -> formats.put(key, createMessageFormat(value, locale)));
            registry.registerAll(locale, formats);
        }

        @SuppressWarnings({"UnstableApiUsage", "deprecation"})
        public void install() {
            if (Tools.requireBukkitVersion(BukkitVersion.V1_19)) {
                GlobalTranslator.translator().addSource(registry);
            } else {
                GlobalTranslator.get().addSource(registry);
            }
        }

        private MessageFormat createMessageFormat(String value, Locale locale) {
            return new MessageFormat(convertToMessageFormatPattern(value), locale);
        }

        private String convertToMessageFormatPattern(String value) {
            StringBuilder builder = new StringBuilder();
            Matcher matcher = FORMAT_PATTERN.matcher(value);
            int nextAutoIndex = 0;
            int start = 0;

            while (matcher.find()) {
                builder.append(escapeMessageFormat(value.substring(start, matcher.start())));

                String explicitIndex = matcher.group(1);
                String conversion = matcher.group(2);

                if (conversion.equals("%")) {
                    builder.append("%");
                } else {
                    int index = explicitIndex != null ? Integer.parseInt(explicitIndex) - 1 : nextAutoIndex++;
                    builder.append("{").append(index).append("}");
                }

                start = matcher.end();
            }

            builder.append(escapeMessageFormat(value.substring(start)));
            return builder.toString();
        }

        private String escapeMessageFormat(String value) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < value.length(); i++) {
                char character = value.charAt(i);

                if (character == '\'') {
                    builder.append("''");
                    continue;
                }
                if (character == '{') {
                    builder.append("'{'");
                    continue;
                }
                if (character == '}') {
                    builder.append("'}'");
                    continue;
                }

                builder.append(character);
            }

            return builder.toString();
        }
    }
}
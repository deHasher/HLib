package net.dehasher.hlib;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import net.dehasher.hlib.data.BukkitVersion;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Rusificator {
    @Getter
    private static final Map<Character, Character> map = createMap();

    private static final Locale DEFAULT_LOCALE = Locale.forLanguageTag("ru-RU");
    private static final String VERSION_MANIFEST = "${url_repos}/minecraft/version_manifest.json";
    private static final String RESOURCE_DOWNLOAD_URL = "https://resources.download.minecraft.net/%s/%s";
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?(?:[-#+ 0,(<]*)?(?:\\d+)?(?:\\.\\d+)?([A-Za-z%])");

    public static synchronized void init() {
        Scheduler.doAsync(() -> {
            try {
                String version = Tools.getBukkitVersion();

                Map<Locale, Map<String, String>> localeTranslations = loadAllLocaleTranslations(version);
                if (localeTranslations.isEmpty()) throw new IllegalStateException("Minecraft locales not loaded for version: " + version);

                VanillaTranslationRegistry newRegistry = new VanillaTranslationRegistry();
                newRegistry.setDefaultLocale(DEFAULT_LOCALE);
                localeTranslations.forEach(newRegistry::registerLocale);
                newRegistry.install();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public static String getMaterialName(Material material, CommandSender sender) {
        if (sender instanceof Player player) {
            return getMaterialName(material, resolveLocale(player));
        } else {
            return getMaterialName(material);
        }
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
        if (sender instanceof Player player) {
            return getBlockName(block, resolveLocale(player));
        } else {
            return getBlockName(block);
        }
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
        if (sender instanceof Player player) {
            return getName(translationKey, resolveLocale(player));
        } else {
            return getName(translationKey);
        }
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

    public static String replace(String input) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            Character character = getMap().get(input.charAt(i));
            builder.append(character != null ? character : input.charAt(i));
        }
        return builder.toString();
    }

    private static Map<Locale, Map<String, String>> loadAllLocaleTranslations(String minecraftVersion) {
        JsonObject versionManifest = readJsonObject(VERSION_MANIFEST);
        String versionMetadataUrl = findVersionMetadataUrl(versionManifest, minecraftVersion);
        JsonObject versionMetadata = readJsonObject(versionMetadataUrl);
        JsonObject assetIndex = readJsonObject(versionMetadata.getAsJsonObject("assetIndex").get("url").getAsString());
        JsonObject objects = assetIndex.getAsJsonObject("objects");

        Map<Locale, Map<String, String>> locales = new ConcurrentHashMap<>();
        objects.entrySet().forEach(entry -> {
            String assetPath = entry.getKey();
            if (!assetPath.startsWith("minecraft/lang/") || !assetPath.endsWith(".json")) return;

            String minecraftLocale = extractMinecraftLocale(assetPath);
            Locale locale = toLocale(minecraftLocale);
            String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
            String localeUrl = buildResourceUrl(hash);
            Map<String, String> translations = parseTranslations(readText(localeUrl));

            if (!translations.isEmpty()) locales.put(locale, translations);
        });

        return locales;
    }

    private static String findVersionMetadataUrl(JsonObject versionManifest, String minecraftVersion) {
        for (JsonElement element : versionManifest.getAsJsonArray("versions")) {
            JsonObject version = element.getAsJsonObject();
            if (minecraftVersion.equals(version.get("id").getAsString())) return version.get("url").getAsString();
        }
        throw new IllegalArgumentException("Minecraft version not found in Mojang manifest: " + minecraftVersion);
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

    private static String extractMinecraftLocale(String assetPath) {
        String fileName = assetPath.substring(assetPath.lastIndexOf("/") + 1);
        return fileName.substring(0, fileName.length() - ".json".length()).toLowerCase();
    }

    private static Locale toLocale(String minecraftLocale) {
        String[] parts = minecraftLocale.split("_", 3);
        if (parts.length == 1) return new Locale(parts[0]);
        if (parts.length == 2) return new Locale(parts[0], parts[1].toUpperCase());
        return new Locale(parts[0], parts[1].toUpperCase(), parts[2]);
    }

    private static String buildResourceUrl(String hash) {
        return String.format(RESOURCE_DOWNLOAD_URL, hash.substring(0, 2), hash);
    }

    private static JsonObject readJsonObject(String url) {
        String response = readText(url);
        JsonElement element = Tools.getGSON().fromJson(response, JsonElement.class);
        if (!element.isJsonObject()) throw new IllegalStateException("JSON object expected from URL: " + url);
        return element.getAsJsonObject();
    }

    private static String readText(String url) {
        String response = Informer.url(url);
        if (response.isEmpty()) throw new IllegalStateException("Empty response from URL: " + url);
        return response;
    }

    private static Map<String, String> parseTranslations(String json) {
        JsonObject object = Tools.getGSON().fromJson(json, JsonObject.class);
        Map<String, String> translations = new ConcurrentHashMap<>();

        object.entrySet().forEach(entry -> {
            JsonElement value = entry.getValue();
            if (value == null || !value.isJsonPrimitive()) return;
            translations.put(entry.getKey(), value.getAsString());
        });

        return translations;
    }

    private static Map<Character, Character> createMap() {
        Map<Character, Character> keyboardMap = new ConcurrentHashMap<>();

        keyboardMap.put('q', 'й'); keyboardMap.put('w', 'ц');
        keyboardMap.put('e', 'у'); keyboardMap.put('r', 'к');
        keyboardMap.put('t', 'е'); keyboardMap.put('y', 'н');
        keyboardMap.put('u', 'г'); keyboardMap.put('i', 'ш');
        keyboardMap.put('o', 'щ'); keyboardMap.put('p', 'з');
        keyboardMap.put('a', 'ф'); keyboardMap.put('s', 'ы');
        keyboardMap.put('d', 'в'); keyboardMap.put('f', 'а');
        keyboardMap.put('g', 'п'); keyboardMap.put('h', 'р');
        keyboardMap.put('j', 'о'); keyboardMap.put('k', 'л');
        keyboardMap.put('l', 'д'); keyboardMap.put('z', 'я');
        keyboardMap.put('x', 'ч'); keyboardMap.put('c', 'с');
        keyboardMap.put('v', 'м'); keyboardMap.put('b', 'и');
        keyboardMap.put('n', 'т'); keyboardMap.put('m', 'ь');

        keyboardMap.put('Q', 'Й'); keyboardMap.put('W', 'Ц');
        keyboardMap.put('E', 'У'); keyboardMap.put('R', 'К');
        keyboardMap.put('T', 'Е'); keyboardMap.put('Y', 'Н');
        keyboardMap.put('U', 'Г'); keyboardMap.put('I', 'Ш');
        keyboardMap.put('O', 'Щ'); keyboardMap.put('P', 'З');
        keyboardMap.put('A', 'Ф'); keyboardMap.put('S', 'Ы');
        keyboardMap.put('D', 'В'); keyboardMap.put('F', 'А');
        keyboardMap.put('G', 'П'); keyboardMap.put('H', 'Р');
        keyboardMap.put('J', 'О'); keyboardMap.put('K', 'Л');
        keyboardMap.put('L', 'Д'); keyboardMap.put('Z', 'Я');
        keyboardMap.put('X', 'Ч'); keyboardMap.put('C', 'С');
        keyboardMap.put('V', 'М'); keyboardMap.put('B', 'И');
        keyboardMap.put('N', 'Т'); keyboardMap.put('M', 'Ь');

        return keyboardMap;
    }

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
                // Minecraft 1.19+ (Adventure 5.x)
                GlobalTranslator.translator().addSource(registry);
            } else {
                // Minecraft 1.16.5 - 1.18.2 (Adventure 4.x)
                GlobalTranslator.get().addSource(registry);
            }
        }

        @SuppressWarnings({"UnstableApiUsage", "deprecation"})
        public void uninstall() {
            if (Tools.requireBukkitVersion(BukkitVersion.V1_19)) {
                GlobalTranslator.translator().removeSource(registry);
            } else {
                GlobalTranslator.get().removeSource(registry);
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
                    builder.append("{")
                           .append(index)
                           .append("}");
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
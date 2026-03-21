package net.dehasher.hlib.hook;

import com.google.gson.JsonElement;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import io.github.kosmx.emotes.api.events.server.ServerEmoteAPI;
import io.github.kosmx.emotes.server.serializer.UniversalEmoteSerializer;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.platform.bukkit.HLib;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmoteCraftHook {
    private static final Map<String, KeyframeAnimation> CACHE = new ConcurrentHashMap<>();

    public static void playEmote(final Player player, final Emotes emote) {
        playEmote(player, getEmote(emote.name()));
    }

    public static void playEmote(final Player player, final Emotes emote, final boolean force) {
        playEmote(player, getEmote(emote.name()), force);
    }

    public static void playEmote(final Player player, final KeyframeAnimation emote) {
        playEmote(player, emote, true);
    }

    public static void playEmote(final Player player, final KeyframeAnimation emote, final boolean force) {
        final UUID uuid = getUUIDFromPlayer(player);
        if (uuid == null) return;
        ServerEmoteAPI.playEmote(uuid, emote, force);
    }

    public static boolean isEmotePlayed(final Player player) {
        final UUID uuid = getUUIDFromPlayer(player);
        if (uuid == null) return false;
        return ServerEmoteAPI.getPlayedEmote(uuid) != null;
    }

    public static void stopEmote(final Player player) {
        final UUID uuid = getUUIDFromPlayer(player);
        if (uuid == null) return;
        ServerEmoteAPI.playEmote(uuid, null, false);
    }

    public static void reload() {
        reload(HLib.getInstance());
    }

    @SuppressWarnings("DataFlowIssue")
    public static void reload(Plugin plugin) {
        File base = plugin.getDataFolder().getParentFile().getParentFile();
        File emotes = new File(base, EmoteCraftHook.Emotes.class.getSimpleName().toLowerCase());
        if (!emotes.exists() && emotes.mkdir()) Informer.send("The emotes folder has been successfully created.");
        List.of(EmoteCraftHook.Emotes.values()).forEach(emote -> {
            File file = Paths.get(emotes.getAbsolutePath() + Tools.getFileSeparator() + emote.name() + ".json").toFile();
            if (file.exists() && !file.delete()) {
                Informer.send("An error occurred when deleting the previous copy of the emote: " + emote.name());
                return;
            }
            // Тут незя юзать Tools.getFileSeparator() :(
            try (InputStream inputStream = plugin.getResource(Tools.join("/", EmoteCraftHook.Emotes.class.getSimpleName().toLowerCase(), emote.name() + ".json"))) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                        stringBuilder.append(System.lineSeparator());
                    }

                    Files.writeString(file.toPath(), stringBuilder.toString());
                    Informer.send("Emote " + emote.name() + " was successful loaded!");
                } catch (Throwable t) {
                    Informer.send("An error occurred while creating the file: " + file.getAbsolutePath());
                    t.printStackTrace();
                }
            } catch (Throwable t) {
                Informer.send("An error occurred while try accessing the file: " + file.getAbsolutePath());
                t.printStackTrace();
            }
        });
        UniversalEmoteSerializer.loadEmotes();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEmotecraftPlayer(final Player player) {
        if (player == null || !player.isOnline()) return false;
        return player.getListeningPluginChannels().contains("emotecraft:emote");
    }

    public static KeyframeAnimation getEmote(final String id) {
        if (id == null || id.isEmpty()) return null;
        if (CACHE.containsKey(id)) return CACHE.get(id);
        CACHE.put(id, ServerEmoteAPI.getLoadedEmotes().values().stream()
                .filter(emote -> emote.extraData.containsKey("name"))
                .filter(emote -> textToString(emote.extraData.get("name")).equalsIgnoreCase(id))
                .findFirst().orElse(null));
        return CACHE.get(id);
    }

    private static String fromJson(final String str) {
        BaseComponent[] components = ComponentSerializer.parse(str);
        return Stream.of(components)
                .map(baseComponent -> baseComponent.toPlainText())
                .collect(Collectors.joining("-"));
    }

    private static String textToString(final Object text) {
        if (text == null) return "";
        if (text instanceof JsonElement json) {
            return fromJson(json.toString());
        }
        if (text instanceof String) {
            try {
                return fromJson((String) text);
            } catch(Exception ignore){}
            return (String) text;
        }
        return "";
    }

    // Миллиардная попытка исправить NPE от Player'a...
    private static UUID getUUIDFromPlayer(final Player player) {
        if (player == null || !player.isOnline()) return null;
        return UUID.fromString(player.getUniqueId().toString());
    }

    public enum Emotes {
        PISS, KISS, CRY, VOMIT, RF, RV
    }
}
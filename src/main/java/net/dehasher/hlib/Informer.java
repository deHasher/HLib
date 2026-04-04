package net.dehasher.hlib;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import net.dehasher.hlib.data.BukkitVersion;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.data.Plugin;
import net.dehasher.hlib.hook.PlaceholderAPIHook;
import net.dehasher.hlib.platform.velocity.HLib;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

// При отправке сообщений в чат через прокси - необходимо наличие плагина на всех серверах.
public class Informer {
    public static final String CHANNEL = Tools.join(":", "${lib_name_id}", "main");
    public static final String ANNOUNCER = "[ANNOUNCER]";

    public static String parse(String message) {
        return Colors.set(Tools.replacePlaceholders(message));
    }

    public static Component parseComponent(String message) {
        Component component = Colors.setComponent(Tools.replacePlaceholders(message));
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_20)) return component;
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> parseComponent(List<String> message) {
        List<Component> result = Lists.newArrayList();
        message.forEach(row -> result.add(parseComponent(Tools.replacePlaceholders(row))));
        return result;
    }

    private static class Bukkit {
        private static void requestChat(org.bukkit.command.CommandSender sender, Object message) {
            if (sender == null) {
                console((String) message);
                return;
            }
            if (message instanceof Component) {
                sender.sendMessage((Component) message);
            } else {
                message = parse(String.valueOf(message));
                if (sender instanceof org.bukkit.entity.Player && Plugin.PLACEHOLDER_API.isEnabled()) {
                    message = PlaceholderAPIHook.setPlaceholders((org.bukkit.entity.Player) sender, (String) message);
                }
                sender.sendMessage((String) message);
            }
        }

        private static void requestTitle(org.bukkit.entity.Player player, String message, int fadeIn, int stay, int fadeOut) {
            if (player == null) return;
            message = parse(message);
            if (Plugin.PLACEHOLDER_API.isEnabled()) {
                message = PlaceholderAPIHook.setPlaceholders(player, message);
            }

            String[] parts = Params.splitTitle(message);
            if (parts.length < 2) return;
            player.sendTitle(parts[0], parts[1], fadeIn, stay, fadeOut);
        }

        private static void requestActionBar(org.bukkit.entity.Player player, String message) {
            if (player == null) return;
            message = parse(message);
            if (Plugin.PLACEHOLDER_API.isEnabled()) message = PlaceholderAPIHook.setPlaceholders(player, message);
            player.sendActionBar(component(message));
        }

        private static void console(String message) {
            message = Colors.clear(message).replace("{prefix}", "");
            org.bukkit.Bukkit.getLogger().info(message);
        }

        private static void broadcast(String message) {
            org.bukkit.Bukkit.getOnlinePlayers().forEach(player -> Bukkit.requestChat(player, message));
        }
    }

    private static class Velocity {
        @Getter
        private static final Logger logger = LoggerFactory.getLogger(Informer.class.getSimpleName());

        @SuppressWarnings({"UnstableApiUsage", "DuplicatedCode"})
        private static void requestChat(com.velocitypowered.api.command.CommandSource source, Object message) {
            if (source == null) {
                console((String) message);
                return;
            }
            if (message instanceof Component) {
                source.sendMessage((Component) message);
            } else {
                if (source instanceof com.velocitypowered.api.proxy.Player player) {
                    if (!player.isActive() || player.getCurrentServer().isEmpty()) return;
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("message");
                    out.writeUTF(parse(String.valueOf(message)));
                    com.velocitypowered.api.proxy.ServerConnection server = player.getCurrentServer().orElse(null);
                    if (server == null) return;
                    server.sendPluginMessage(HLib.getChannelIdentifier(), out.toByteArray());
                } else {
                    source.sendMessage(component(Colors.clear(parse(String.valueOf(message)))));
                }
            }
        }

        private static void requestTitle(com.velocitypowered.api.proxy.Player player, String message, int fadeIn, int stay, int fadeOut) {
            if (player == null) return;
            message = parse(message);

            String[] parts = Params.splitTitle(message);
            if (parts.length < 2) return;
            player.showTitle(Title.title(
                    component(parts[0]),
                    component(parts[1]),
                    Title.Times.times(
                            Duration.ofMillis(fadeIn * 50L),
                            Duration.ofMillis(stay * 50L),
                            Duration.ofMillis(fadeOut * 50L)
                    )
            ));
        }

        private static void requestActionBar(com.velocitypowered.api.proxy.Player player, String message) {
            if (player == null) return;
            message = parse(message);
            player.sendActionBar(component(message));
        }

        private static void console(String message) {
            message = Colors.clear(message).replace("{prefix}", "");
            getLogger().info(message);
        }

        private static void broadcast(String message) {
            HLib.getProxy().getAllPlayers().forEach(player -> requestChat(player, message));
        }
    }

    private static class Params {
        private static String[] splitTitle(String message) {
            if (message == null || message.isEmpty()) return new String[0];
            String[] parts = message.split("\n", 2);
            if (parts.length == 1) return new String[]{parts[0], ""};
            return parts;
        }
    }

    public static Component component(String message) {
        return parseComponent(message != null ? message : "");
    }

    public static void send(Object... objects) {
        switch (objects.length) {
            case 1:
                switch (Platform.get()) {
                    case BUKKIT -> Bukkit.console(String.valueOf(objects[0]));
                    case VELOCITY -> Velocity.console(String.valueOf(objects[0]));
                }
                break;
            case 2:
                Object player  = objects[0];
                Object message = objects[1];
                switch (Platform.get()) {
                    case BUKKIT:
                        if (player instanceof org.bukkit.command.CommandSender) {
                            Bukkit.requestChat((org.bukkit.command.CommandSender) player, message);
                        }
                        break;
                    case VELOCITY:
                        if (player instanceof com.velocitypowered.api.command.CommandSource) {
                            Velocity.requestChat((com.velocitypowered.api.command.CommandSource) player, message);
                        }
                        break;
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void broadcast(Object message) {
        switch (Platform.get()) {
            case BUKKIT -> Bukkit.broadcast(String.valueOf(message));
            case VELOCITY -> Velocity.broadcast(String.valueOf(message));
        }
    }

    public static void sendTitle(Object... objects) {
        if (objects.length < 2 || objects.length > 5) throw new IllegalArgumentException();

        Object player  = objects[0];
        Object message = objects[1];

        int fadeIn     = objects.length == 5 ? (int) objects[2] : 20;
        int stay       = objects.length == 5 ? (int) objects[3] : 100;
        int fadeOut    = objects.length == 5 ? (int) objects[4] : 20;

        switch (Platform.get()) {
            case BUKKIT:
                if (player instanceof org.bukkit.entity.Player) {
                    Bukkit.requestTitle((org.bukkit.entity.Player) player, String.valueOf(message), fadeIn, stay, fadeOut);
                }
                break;
            case VELOCITY:
                if (player instanceof com.velocitypowered.api.proxy.Player) {
                    Velocity.requestTitle((com.velocitypowered.api.proxy.Player) player, String.valueOf(message), fadeIn, stay, fadeOut);
                }
                break;
        }
    }

    public static void sendActionBar(Object... objects) {
        if (objects.length != 2) throw new IllegalArgumentException();
        Object player  = objects[0];
        Object message = objects[1];
        switch (Platform.get()) {
            case BUKKIT:
                if (player instanceof org.bukkit.entity.Player) {
                    Bukkit.requestActionBar((org.bukkit.entity.Player) player, String.valueOf(message));
                }
                break;
            case VELOCITY:
                if (player instanceof com.velocitypowered.api.proxy.Player) {
                    Velocity.requestActionBar((com.velocitypowered.api.proxy.Player) player, String.valueOf(message));
                }
                break;
        }
    }

    public static class BossBar {
        public enum Color {
            PINK,
            BLUE,
            RED,
            GREEN,
            YELLOW,
            PURPLE,
            WHITE
        }

        @Getter
        public enum Style {
            SOLID("SOLID", "PROGRESS"),
            SPLIT_6("SEGMENTED_6", "NOTCHED_6"),
            SPLIT_10("SEGMENTED_10", "NOTCHED_10"),
            SPLIT_12("SEGMENTED_12", "NOTCHED_12"),
            SPLIT_20("SEGMENTED_20", "NOTCHED_20");

            private final String bukkit;
            private final String velocity;

            Style(String bukkit, String velocity) {
                this.bukkit = bukkit;
                this.velocity = velocity;
            }
        }

        public static org.bukkit.boss.BossBar createBukkit(String title, BossBar.Color color, BossBar.Style style, double progress) {
            org.bukkit.boss.BossBar bar = org.bukkit.Bukkit.createBossBar(title, org.bukkit.boss.BarColor.valueOf(color.name()), org.bukkit.boss.BarStyle.valueOf(style.getBukkit()));
            bar.setTitle(title);
            bar.setProgress(progress);
            return bar;
        }

        public static net.kyori.adventure.bossbar.BossBar createVelocity(String title, BossBar.Color color, BossBar.Style style, double progress) {
            return net.kyori.adventure.bossbar.BossBar.bossBar(component(title), (float) progress, net.kyori.adventure.bossbar.BossBar.Color.valueOf(color.name()), net.kyori.adventure.bossbar.BossBar.Overlay.valueOf(style.getVelocity()));
        }
    }

    public static String url(String link) {
        return url(link, (byte[]) null, HttpMethod.GET, 3);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static String url(String link, Map<String, String> params, HttpMethod httpMethod) {
        return url(link, params, httpMethod, 3);
    }

    public static String url(String link, String json, HttpMethod httpMethod) {
        return url(link, json, httpMethod, 3);
    }

    public static String url(String link, Map<String, String> params, HttpMethod httpMethod, int timeout) {
        return url(link, params != null ? Tools.httpBuildQuery(params).getBytes(StandardCharsets.UTF_8) : null, httpMethod, timeout);
    }

    public static String url(String link, String json, HttpMethod httpMethod, int timeout) {
        return url(link, json != null ? json.getBytes(StandardCharsets.UTF_8) : null, httpMethod, timeout);
    }

    @SuppressWarnings("DuplicatedCode")
    public static String url(String link, byte[] bytes, HttpMethod httpMethod, int timeout) {
        try {
            link = Tools.replacePlaceholders(link);
            URL url = new URL(link);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout * 1000);
            connection.setReadTimeout(timeout * 1000);
            connection.setRequestMethod(httpMethod == HttpMethod.JSON ? HttpMethod.POST.name() : httpMethod.name());
            connection.setRequestProperty("User-Agent", "Chrome");
            if (bytes != null) {
                if (httpMethod == HttpMethod.JSON) connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write((byte[]) bytes);
                }
            }

            InputStream inputStream = connection.getResponseCode() >= 400 ? connection.getErrorStream() : connection.getInputStream();
            if (inputStream == null) return "";

            StringBuilder response = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) response.append(line);
            br.close();
            return response.toString();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return "";
    }

    public static Map<String, List<String>> head(String link) {
        return head(link, 3);
    }

    @SuppressWarnings("DuplicatedCode")
    public static Map<String, List<String>> head(String link, int timeout) {
        try {
            link = Tools.replacePlaceholders(link);
            URL url = new URL(link);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(timeout * 1000);
            connection.setReadTimeout(timeout * 1000);
            connection.setRequestMethod(HttpMethod.HEAD.name());
            connection.setRequestProperty("User-Agent", "Chrome");

            int code = connection.getResponseCode();
            if (code >= 400) return null;
            return connection.getHeaderFields();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    public enum HttpMethod { GET, POST, JSON, HEAD }
}
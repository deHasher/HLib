package net.dehasher.hlib;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.controller.DNSController;
import net.dehasher.hlib.controller.DebugController;
import net.dehasher.hlib.data.*;
import net.dehasher.hlib.database.MySQL;
import net.dehasher.hlib.database.Redis;
import net.dehasher.hlib.file.Configuration;
import net.dehasher.hlib.file.ConfigurationProvider;
import net.dehasher.hlib.file.PluginCfg;
import net.dehasher.hlib.file.provider.StandaloneConfigurationProvider;
import net.dehasher.hlib.platform.velocity.HLib;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tools {
    @Getter
    private static final Gson GSON = new GsonBuilder().create();
    @Getter
    private final static boolean devMode = parseDevMode();
    @Getter
    private static final boolean strictServer = parseStrictServer();
    @Getter
    private static final String bukkitVersion = parseBukkitVersion();
    @Getter
    private static final String velocityVersion = parseVelocityVersion();
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Boolean anarchy = null;
    @Getter(AccessLevel.PRIVATE)
    @Setter
    private static MySQL mysqlInstance = null;
    @Getter(AccessLevel.PRIVATE)
    @Setter
    private static Redis redisInstance = null;
    @Getter
    @Setter
    private static boolean HCoreEnabled = false;

    @Getter
    private static final List<String> nameOwners = List.of(
            Encrypt.DEHASHER.value,
            Encrypt.FLUGEGEHEIMEN.value,
            Encrypt.MUASO.value
    );
    @Getter
    private static final Map<BukkitVersion, Boolean> requireServerVersionCache = new ConcurrentHashMap<>();

    // Проверяем текст на вредоносный код log4j2.
    public static boolean isLog4j(String string) {
        string = Colors.clear(string.toLowerCase().replaceAll("[^\\x00-\\x7F]", ""));
        if (!string.contains("${")) return false;
        return CompiledPattern.LOG4J.find(string);
    }

    // Получаем онлайн сервера.
    public static int getOnline() {
        if (Platform.get().isProxy()) {
            return HLib.getProxy().getPlayerCount();
        } else {
            return org.bukkit.Bukkit.getOnlinePlayers().size();
        }
    }

    // Получаем фейковый онлайн сервера.
    public static int getFakeOnline(int online) {
        int add = Math.round(online * (float) (Tools.parseInt(Info.FakeOnline.percent.replace("%", ""))) / (float) 100);
        int random = Info.FakeOnline.needRandomize && online > 0 ? Tools.getRandomInt(1, 9) : 0;
        return online + add + random;
    }

    // Заменяем все плейсхолдеры.
    public static String replacePlaceholders(String input) {
        return input
                .replace("{prefix}", Info.Placeholders.prefix)
                .replace("{site_url}", Info.Placeholders.site_url);
    }

    // Получить версию баккита.
    private static String parseBukkitVersion() {
        if (Platform.get().isProxy()) return null;
        String[] version = org.bukkit.Bukkit.getBukkitVersion().split("-")[0].split("\\.");
        if (version.length < 2) return "1.0";
        return version[0] + "." + version[1];
    }

    // Получить версию прокси.
    private static String parseVelocityVersion() {
        if (!Platform.get().isProxy()) return null;
        return HLib.getProxy().getVersion().getVersion().split("-")[0].split(" ")[0]; // 2 сплита на всякий...
    }

    // Получить MySQL.
    public static MySQL getMySQL() {
        MySQL mysql = getMysqlInstance();
        return mysql != null && mysql.isEnabled() ? mysql : null;
    }

    // Получить Redis.
    public static Redis getRedis() {
        Redis data = getRedisInstance();
        if (data == null || !data.isEnabled()) return null;
        try (redis.clients.jedis.Jedis jedis = data.getPool().getResource()) {
            if (jedis == null) {
                data.shutdown();
                return null;
            }
        }
        return data;
    }

    // Смэрть!
    public static void shutdown() {
        if (Platform.get().isProxy()) {
            HLib.getProxy().shutdown();
        } else {
            org.bukkit.Bukkit.shutdown();
        }
    }

    // Отправляем отладочное сообщение.
    public static void debug(Object player, Object message) {
        if (!DebugController.isEnabled()) return;
        DebugController.getPlayers().forEach(name -> {
            if (name.equalsIgnoreCase(DebugController.getConsoleName()) && player == null) {
                Informer.send(message);
                return;
            }
            org.bukkit.entity.Player debug = (org.bukkit.entity.Player) player;
            if (debug == null || !debug.isOnline()) return;
            if (!debug.getName().equalsIgnoreCase(name)) return;
            Informer.send(debug, message);
        });
    }

    // Получаем название сервера.
    public static String getServerName() {
        return Info.Server.name;
    }

    // Получаем уникальный идентификатор сервера.
    public static String getServerID() {
        return Info.Server.name + "-" + Info.Server.id;
    }

    // Получить IP игрока.
    public static String getPlayerIP(org.bukkit.entity.Player player) {
        @Nullable InetSocketAddress address = player.getAddress();
        if (address == null) return null;
        return address.getAddress().getHostAddress();
    }

    // Получить IP игрока.
    public static String getPlayerIP(org.bukkit.command.CommandSender player) {
        return getPlayerIP((org.bukkit.entity.Player) player);
    }

    // Получить IP игрока.
    public static String getPlayerIP(com.velocitypowered.api.proxy.Player player) {
        return player.getRemoteAddress().getAddress().getHostAddress();
    }

    // Получить IP игрока.
    public static String getPlayerIP(com.velocitypowered.api.proxy.InboundConnection player) {
        return player.getRemoteAddress().getAddress().getHostAddress();
    }

    // Получить IP домена.
    public static String getDomainIP(String domain) {
        try {
            return InetAddress.getByName(domain).getHostAddress();
        } catch (Throwable ignored) {}
        return null;
    }

    // Проверка игрока на наличие прав.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPerm(org.bukkit.command.CommandSender sender, Permission permission, Object... custom) {
        return !(sender instanceof org.bukkit.entity.Player) || isPerm((org.bukkit.entity.Player) sender, permission, custom);
    }

    // Проверка игрока на наличие прав.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPerm(com.velocitypowered.api.command.CommandSource sender, Permission permission, Object... custom) {
        return !(sender instanceof com.velocitypowered.api.proxy.Player) || isPerm((com.velocitypowered.api.proxy.Player) sender, permission, custom);
    }

    // Проверка игрока на наличие прав.
    public static boolean isPerm(org.bukkit.entity.Player player, Permission permission, Object... custom) {
        if (player == null) return false;
        if (permission != null) {
            if (permission.isStrict() && isStrictServer() && !isOwner(player)) return false;
            if (player.hasPermission(permission.getValue() + generatePerm(custom))) return true;
            if (permission.isAdminSkip()) return false;
        }
        return isAdmin(Info.admins, player.getName());
    }

    // Проверка игрока на наличие прав.
    public static boolean isPerm(com.velocitypowered.api.proxy.Player player, Permission permission, Object... custom) {
        if (player == null) return false;
        if (permission != null) {
            if (permission.isStrict() && isStrictServer() && !isOwner(player)) return false;
            if (player.hasPermission(permission.getValue() + generatePerm(custom))) return true;
            if (permission.isAdminSkip()) return false;
        }
        return isAdmin(Info.admins, player.getUsername());
    }

    // Генерируем опциональные данные права.
    private static String generatePerm(Object... custom) {
        StringBuilder result = new StringBuilder();
        if (custom != null && custom.length > 0) List.of(custom).forEach(object -> result.append(".").append(object));
        return result.toString();
    }

    // Округление double числа.
    public static double round(float number, int symbols) {
        return round((double) number, symbols);
    }

    // Округление double числа.
    public static double round(double number, int symbols) {
        double math = 1d;
        for (int i = 0; i < symbols; i++) math *= 10;
        return (double) Math.round(number * math) / math;
    }

    // Округление double числа в меньшую сторону.
    public static double floor(float number, int symbols) {
        return floor((double) number, symbols);
    }

    // Округление double числа в меньшую сторону.
    public static double floor(double number, int symbols) {
        double math = 1d;
        for (int i = 0; i < symbols; i++) math *= 10;
        return Math.floor(number * math) / math;
    }

    // Округление double числа в большую сторону.
    public static double ceil(float number, int symbols) {
        return ceil((double) number, symbols);
    }

    // Округление double числа в большую сторону.
    public static double ceil(double number, int symbols) {
        double math = 1d;
        for (int i = 0; i < symbols; i++) math *= 10;
        return Math.ceil(number * math) / math;
    }

    // Получить ближайшие блоки в радиусе.
    public static List<org.bukkit.block.Block> getNearbyBlocks(org.bukkit.Location location, int radius) {
        List<org.bukkit.block.Block> blocks = Lists.newArrayList();
        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

    // Телепортация игрока.
    public static void teleportPlayer(org.bukkit.entity.Player player, org.bukkit.Location loc) {
        player.teleport(loc);
    }

    // Проверка на админку.
    private static boolean isAdmin(List<String> list, String name) {
        return list.stream().anyMatch(user -> user.equalsIgnoreCase(name));
    }

    // Проверка на владельца.
    public static boolean isOwner(org.bukkit.entity.Player player) {
        return getNameOwners().contains(player.getName());
    }

    // Проверка на владельца.
    private static boolean isOwner(com.velocitypowered.api.proxy.Player player) {
        return getNameOwners().contains(player.getUsername());
    }

    // Получить разделитель файлов.
    public static String getFileSeparator() {
        return File.separator;
    }

    // Получить разделитель строк.
    public static String getLineSeparator() {
        return System.lineSeparator();
    }

    // Проверяем строку на кириллицу.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isCyrillic(String input) {
        return CompiledPattern.CYRILLIC.matches(input);
    }

    // Генерируем массив get/post.
    public static String httpBuildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                    .map(p -> urlEncode(p.getKey()) + "=" + urlEncode((p.getValue())))
                    .reduce((p1, p2) -> p1 + "&" + p2).orElse("");
    }

    // Шифруем строку в url.
    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return input;
        }
    }

    // Расшифровываем url в строку.
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            return input;
        }
    }

    // Подсчёт байт в предмете.
    public static int checkBytes(org.bukkit.inventory.ItemStack item) {
        return item.toString().getBytes(StandardCharsets.UTF_8).length;
    }

    // Получить случайный int в заданном диапазоне.
    public static int getRandomInt(int min, int max) {
        return (int) ((Math.random() * ((max + 1) - min)) + min);
    }

    // return provided >= needed ? true : false;
    public static boolean compareVersions(@Nullable String provided, @Nullable String needed) {
        if (provided == null || needed == null) return false;

        String[] providedParts = provided.split("\\.");
        String[] neededParts   = needed.split("\\.");

        int i = 0;
        while (i < providedParts.length && i < neededParts.length && providedParts[i].equals(neededParts[i])) i++;

        if (i < providedParts.length && i < neededParts.length) {
            if (providedParts[i].isEmpty() || neededParts[i].isEmpty()) return false;
            int diff = Integer.valueOf(providedParts[i]).compareTo(Integer.valueOf(neededParts[i]));
            return Integer.signum(diff) >= 0;
        }

        return Integer.signum(providedParts.length - neededParts.length) >= 0;
    }

    // Проверка версии ядра на заданную.
    public static boolean requireBukkitVersion(BukkitVersion needed) {
        if (Platform.get().isProxy()) return true;
        return getRequireServerVersionCache().computeIfAbsent(needed, key -> compareVersions(getBukkitVersion(), key.getValue()));
    }

    // Проверка на винду.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // Объединить аргументы.
    public static String argumentsAppend(String[] arguments) {
        StringBuilder message = new StringBuilder();
        List.of(arguments).forEach(string -> message.append(" ").append(string));
        return message.toString().trim();
    }

    // Проверка ника на корректность.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean validateNickname(String player) {
        return CompiledPattern.PLAYER.matches(player);
    }

    // Проверка айпи на корректность.
    public static boolean validateIP(String ip) {
        return CompiledPattern.IP.matches(ip);
    }

    // Удаляем последние символы в строке.
    public static String removeLastChars(String input, int chars) {
        return input.substring(0, input.length() - chars);
    }

    // Делаем первую букву заглавной.
    public static String firstUpperCase(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }

    // Перезагружаем файлы конфигурации.
    public static <T extends Enum<T> & PluginCfg> void reloadFiles(Path dataFolder, Class<T> cfgClass) {
        reloadFiles(dataFolder.toFile(), cfgClass);
    }

    // Перезагружаем файлы конфигурации.
    @SuppressWarnings("unchecked")
    public static <T extends Enum<T> & PluginCfg> void reloadFiles(File dataFolder, Class<T> cfgClass) {
        if (!dataFolder.exists()) {
            if (!dataFolder.mkdirs()) {
                Informer.send("The plugin folder could not be created. Check your chmod permissions!");
            }
        }

        List.of(cfgClass.getEnumConstants()).forEach(cfg -> {
            if (cfg.name().toLowerCase().startsWith(Platform.get().isProxy() ? "bukkit" : "proxy")) return; // Не перезагружаем файлы прокси у баккита и наоборот.
            ClassLoader classLoader = cfgClass.getClassLoader();
            Class<? extends Configuration> configClass = (Class<? extends Configuration>) Stream.of(cfgClass.getPackageName() + ".config." + cfg.name(), cfgClass.getPackageName() + "." + cfg.name())
                    .flatMap(className -> {
                        try {
                            return Stream.of(classLoader.loadClass(className));
                        } catch (Throwable ignored) {
                            return Stream.empty();
                        }
                    })
                    .findFirst()
                    .orElse(null);

            if (configClass != null) {
                Configuration config = Configuration.builder(configClass)
                        .file(new File(dataFolder, cfg.getFile()))
                        .provider(StandaloneConfigurationProvider.class).build();
                cfg.setConfig(config);
                setupConfigFile(cfg, config);
            }
        });
    }

    // Настраиваем конфигурационные файлы.
    public static void setupConfigFile(PluginCfg cfg, Configuration config) {
        ConfigurationProvider provider = config.getConfigurationProvider();
        provider.reloadFileFromDisk();
        File file = provider.getConfigFile();

        if (file.exists() && provider.get("version") == null) {
            try {
                java.nio.file.Files.move(file.toPath(), (new File(file.getParentFile(), cfg.getFile() + ".old." + System.nanoTime())).toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
            provider.reloadFileFromDisk();
        }
        if (!file.exists()) {
            config.save();
            config.load();
            cfg.reload(provider);
            Informer.send("File " + cfg.getFile() + " successfully created and loaded. v" + cfg.getVersion());
        } else if (provider.isFileSuccessfullyLoaded()) {
            if (config.load()) {
                cfg.reload(provider);
                Informer.send("File " + cfg.getFile() + " successfully loaded. v" + cfg.getVersion());
            } else {
                Informer.send("Failed to load " + cfg.getFile() + " config :\\");
            }
        } else {
            Informer.send("Can't load settings from file, using default...");
        }
    }

    // Получаем игрока, котороый нанёс урон.
    public static org.bukkit.entity.Player getDamager(org.bukkit.entity.Entity entity) {
        org.bukkit.entity.Player player = null;
        if (entity instanceof org.bukkit.entity.Player) {
            player = (org.bukkit.entity.Player) entity;
        } else if (entity instanceof org.bukkit.entity.Projectile projectile) {
            if (entity instanceof org.bukkit.entity.Firework) {
                if (entity.hasMetadata("arrow-firework-shooter")) {
                    org.bukkit.metadata.MetadataValue metadata = entity.getMetadata("arrow-firework-shooter").stream()
                            .filter(metadataValue -> metadataValue.getOwningPlugin() == net.dehasher.hlib.platform.bukkit.HLib.getInstance())
                            .findFirst()
                            .orElse(null);
                    if (metadata != null) {
                        @Nullable Plugin owningPlugin = metadata.getOwningPlugin();
                        @Nullable Object value = metadata.value();
                        if (owningPlugin != null) entity.removeMetadata("arrow-firework-shooter", owningPlugin);
                        if (value != null) player = org.bukkit.Bukkit.getPlayer((UUID) value);
                    }
                }
            } else {
                if (projectile.getShooter() instanceof org.bukkit.entity.Player) player = (org.bukkit.entity.Player) projectile.getShooter();
            }
        } else if (entity instanceof org.bukkit.entity.TNTPrimed tntPrimed) {
            player = getDamager(tntPrimed.getSource());
        } else if (entity instanceof org.bukkit.entity.AreaEffectCloud areaEffectCloud) {
            if (areaEffectCloud.getSource() instanceof org.bukkit.entity.Player) player = (org.bukkit.entity.Player) areaEffectCloud.getSource();
        }
        return player;
    }

    // Регистрируем слушатель.
    public static void registerListener(Object listener, Object plugin) {
        if (Platform.get().isProxy()) {
            HLib.getProxy().getEventManager().register(plugin, listener);
        } else {
            org.bukkit.Bukkit.getPluginManager().registerEvents((org.bukkit.event.Listener) listener, (org.bukkit.plugin.Plugin) plugin);
        }
    }

    // Регистрируем команду.
    public static void registerCommand(Object plugin, String command, List<String> aliases, int cooldown, int limit) {
        registerCommand(plugin, command, aliases, cooldown, limit, null, null, null, false);
    }

    // Регистрируем команду.
    public static void registerCommand(Object plugin, String command, List<String> aliases, int cooldown, int limit, ClassLoader classLoader, String packagePath, String className, boolean customPath) {
        try {
            if (classLoader == null) classLoader = plugin.getClass().getClassLoader();
            if (packagePath == null) packagePath = plugin.getClass().getPackage().getName();
            if (className == null) className = command;
            Class<?> clazz = classLoader.loadClass(join(customPath ? "." : ".command.", packagePath, className));
            Constructor<?> constructor = clazz.getDeclaredConstructor(Object.class, String.class, List.class, int.class, int.class);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(plugin, command, aliases, cooldown, limit);
            if (Platform.get().isProxy()) {
                HLib.getProxy().getCommandManager().register(HLib.getProxy().getCommandManager()
                        .metaBuilder(command)
                        .aliases(aliases.toArray(new String[0]))
                        .plugin(plugin)
                        .build(), (com.velocitypowered.api.command.Command) instance);
            } else {
                Field serverCommandMap = org.bukkit.Bukkit.getServer().getClass().getDeclaredField("commandMap");
                serverCommandMap.setAccessible(true);
                org.bukkit.command.CommandMap commandMap = (org.bukkit.command.CommandMap) serverCommandMap.get(org.bukkit.Bukkit.getServer());
                commandMap.register(plugin.getClass().getSimpleName(), (org.bukkit.command.Command) instance);
            }
            String message = "Command /" + command + " successful registered!";
            if (!aliases.isEmpty()) message += " Aliases: " + aliases;
            if (cooldown > 0) message = message + " Cooldown: " + cooldown + "s";
            if (limit > 0) message = message + " Limit: " + limit;
            Informer.send(message);
        } catch (Throwable t) {
            Informer.send("An error occurred during registering command(" + command + "): " + t.getMessage());
            t.printStackTrace();
        }
    }

    // Читаем текст из файла.
    public static String readFile(File file) {
        String result = "";
        if (!file.exists()) return result;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }

            result = stringBuilder.toString();
        } catch (Throwable ignored) {}
        return result;
    }

    // Получаем онлайн UUID по нику.
    @SuppressWarnings("DuplicatedCode")
    public static UUID onlineUUID(String input) {
        String json = Informer.url("https://api.mojang.com/users/profiles/minecraft/" + input);
        if (json.isEmpty()) return null;

        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(json, JsonObject.class);
        } catch (Throwable t) {
            return null;
        }
        if (jsonObject == null || !jsonObject.has("id")) return null;

        String id = jsonObject.get("id").getAsString();
        String formattedUUID = id.replaceFirst(CompiledPattern.UUID.getValue().pattern(), "$1-$2-$3-$4-$5");

        return UUID.fromString(formattedUUID);
    }

    // Получаем оффлайн UUID по нику.
    public static UUID offlineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    // Сериализация UUID.
    public static String serializeUUID(UUID uuid) {
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        int[] data = new int[]{(int) (mostSignificantBits >> 32), (int) mostSignificantBits, (int) (leastSignificantBits >> 32), (int) leastSignificantBits};
        return String.format("I;%d,%d,%d,%d", data[0], data[1], data[2], data[3]);
    }

    // Получить данные скина игрока.
    @SuppressWarnings({"DuplicatedCode", "SizeReplaceableByIsEmpty"})
    public static JsonObject getPlayerSkinData(UUID input) {
        String uuid = input.toString().replace("-", "").toLowerCase();
        String json = Informer.url("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
        if (json.isEmpty()) return null;

        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(json, JsonObject.class);
        } catch (Throwable t) {
            return null;
        }
        if (jsonObject == null || !jsonObject.has("properties")) return null;

        JsonArray propertiesArray = jsonObject.getAsJsonArray("properties");
        if (propertiesArray == null || propertiesArray.size() == 0) return null;

        JsonObject property = propertiesArray.get(0).getAsJsonObject();
        if (property == null || !property.has("value")) return null;

        String base64 = property.get("value").getAsString();
        if (base64 == null || base64.isEmpty()) return null;

        String decodedJson = base64Decode(base64);
        if (decodedJson == null || decodedJson.isEmpty()) return null;

        try {
            return gson.fromJson(decodedJson, JsonObject.class);
        } catch (Throwable t) {
            return null;
        }
    }

    // Нужно ли повреждать инструмент если событие было отменено?
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean shouldDamageTool(org.bukkit.inventory.ItemStack tool) {
        int unbreakingLevel = tool.getEnchantmentLevel(NMS.UNBREAKING);
        if (unbreakingLevel > 0) {
            double chance = 1.0 / (unbreakingLevel + 1);
            return Math.random() > chance;
        }
        return true;
    }

    // Модифицировать голову.
    @SuppressWarnings({"deprecation", "ExtractMethodRecommender"})
    public static void headModify(org.bukkit.inventory.ItemStack item, UUID uuid, String name, String textures) {
        if (uuid == null) uuid = UUID.randomUUID();
        if (name == null || name.isEmpty()) name = uuid.toString();
        if (textures == null || textures.isEmpty()) textures = "null";
        String encode;

        if (requireBukkitVersion(BukkitVersion.V1_20)) {
            String itemType = item.getType().name().toLowerCase();

            JsonObject texturesObj = new JsonObject();
            texturesObj.addProperty("name", "textures");
            texturesObj.addProperty("value", textures);

            JsonArray texturesArray = new JsonArray();
            texturesArray.add(texturesObj);

            encode = itemType + "[profile={id:[" + serializeUUID(uuid) + "],name:\"" + name + "\",properties:" + texturesArray + "}]";
        } else {
            JsonObject texturesObj = new JsonObject();
            texturesObj.addProperty("Value", textures);

            JsonArray texturesArray = new JsonArray();
            texturesArray.add(texturesObj);

            JsonObject propertiesObj = new JsonObject();
            propertiesObj.add("textures", texturesArray);

            JsonObject skullOwnerObj = new JsonObject();
            skullOwnerObj.addProperty("Id", String.valueOf(uuid));
            skullOwnerObj.add("Properties", propertiesObj);

            JsonObject json = new JsonObject();
            json.add("SkullOwner", skullOwnerObj);

            encode = json.toString();
        }

        debug(null, encode);
        org.bukkit.Bukkit.getUnsafe().modifyItemStack(item, encode);
    }

    // Получаем список игроков рядом с игроком.
    public static List<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.entity.Player player) {
        return getNearbyPlayers(player, (org.bukkit.Bukkit.getViewDistance() - 1) * 16, requireBukkitVersion(BukkitVersion.V1_18) ? 384 : 255);
    }

    // Получаем список игроков рядом с игроком.
    public static List<org.bukkit.entity.Player> getNearbyPlayers(org.bukkit.entity.Player player, int radius, int height) {
        return player
                .getNearbyEntities(radius, height, radius).stream()
                .filter(entity -> entity instanceof org.bukkit.entity.Player)
                .map(entity -> (org.bukkit.entity.Player) entity)
                .collect(Collectors.toList());
    }

    // Получаем список существ рядом с игроком.
    public static List<org.bukkit.entity.Entity> getNearbyEntity(org.bukkit.entity.Player player) {
        return getNearbyEntity(player, (org.bukkit.Bukkit.getViewDistance() - 1) * 16, requireBukkitVersion(BukkitVersion.V1_18) ? 384 : 255);
    }

    // Получаем список существ рядом с игроком.
    public static List<org.bukkit.entity.Entity> getNearbyEntity(org.bukkit.entity.Player player, int radius, int height) {
        return player
                .getNearbyEntities(radius, height, radius).stream()
                .filter(entity -> entity instanceof org.bukkit.entity.Mob || entity instanceof org.bukkit.entity.Player)
                .filter(entity -> !isNPC(entity))
                .collect(Collectors.toList());
    }

    // Спокойной ночи.
    public static void sleep(int millis) {
        sleep((long) millis);
    }

    // Спокойной ночи.
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable ignored) {}
    }

    // Получаем время выполнения скрипта.
    public static long getExecTime(Runnable runnable) {
        long start = System.currentTimeMillis();
        runnable.run();
        return System.currentTimeMillis() - start;
    }

    // Удаляем .DS_Store и desktop.ini файлы.
    public static void removeStyleFiles() {
        AtomicInteger deleted = new AtomicInteger();
        try (Stream<Path> stream = java.nio.file.Files.find(Paths.get(new File("").getPath()), 999, (path, file) -> file.isRegularFile())) {
            stream.filter(e -> e.endsWith(".DS_Store") || e.endsWith("desktop.ini")).forEach(e -> {
                try {
                    java.nio.file.Files.delete(e);
                    deleted.getAndIncrement();
                } catch (Throwable ignored) {}
            });
        } catch (Throwable ignored) {}
        if (deleted.get() > 0) Informer.send("Successfully deleted " + deleted.get() + " .DS_Store and desktop.ini files!");
    }

    // Вычисляем линейное преобразование.
    public static long calculateLinearIncrement(long number, long difference, long count) {
        if (count <= 0) return number;
        return number + difference * count;
    }

    // Вычисляем линейное преобразование.
    public static double calculateLinearIncrement(double number, double difference, double count) {
        if (count <= 0) return number;
        return number + difference * count;
    }

    // Зашифровать в base64.
    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    // Расшифровать из base64.
    public static String base64Decode(String input) {
        try {
            return new String(Base64.getDecoder().decode(input));
        } catch (Throwable t) {
            return null;
        }
    }

    // Получаем таб комплейт.
    public static List<String> getTabComplete(Set<String> data, String[] arguments) {
        return getTabComplete(Lists.newArrayList(data), arguments);
    }

    // Получаем таб комплейт.
    public static List<String> getTabComplete(List<String> data, String[] arguments) {
        if (arguments.length == 0) return data;
        return data.stream()
                .filter(e -> e.toLowerCase().startsWith(arguments[arguments.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    // Получаем таб комплейт.
    public static List<String> getTabComplete(Map<String, String> data, String[] arguments) {
        List<String> result = getTabComplete(data.keySet(), arguments);
        Set<String> set = ConcurrentHashMap.newKeySet();
        set.addAll(data.values());
        result.addAll(getTabComplete(set, arguments));
        return result;
    }

    // Тот же String.join, но с поддержкой всех типов объектов.
    public static String join(CharSequence delimiter, Object... elements) {
        StringJoiner joiner = new StringJoiner(delimiter);
        for (Object o1 : elements) {
            if (o1 instanceof Collection<?>) {
                for (Object o2 : (Collection<?>) o1) joiner.add(String.valueOf(o2));
            } else {
                joiner.add(String.valueOf(o1));
            }
        }
        return joiner.toString();
    }

    // Получить цель игрока.
    public static org.bukkit.entity.Player getTargetPlayer(org.bukkit.entity.Player player) {
        AtomicReference<org.bukkit.entity.Player> targetPlayer = new AtomicReference<>(null);
        org.bukkit.Location playerPos = player.getEyeLocation();
        Vector3D playerDir   = new Vector3D(playerPos.getDirection());
        Vector3D playerStart = new Vector3D(playerPos);
        Vector3D playerEnd   = playerStart.add(playerDir.multiply(100));

        player.getWorld().getPlayers().forEach(all -> {
            Vector3D targetPos = new Vector3D(all.getLocation());
            Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
            Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

            if (all != player && hasIntersection(playerStart, playerEnd, minimum, maximum)) {
                if (targetPlayer.get() == null || targetPlayer.get().getLocation().distanceSquared(playerPos) > all.getLocation().distanceSquared(playerPos)) targetPlayer.set(all);
            }
        });

        return targetPlayer.get();
    }

    // Проверка интерсекции позиций.
    private static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max) {
        double epsilon = 0.0001f;
        Vector3D d = p2.subtract(p1).multiply(0.5);
        Vector3D e = max.subtract(min).multiply(0.5);
        Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
        Vector3D ad = d.abs();

        if (Math.abs(c.x) > e.x + ad.x) return false;
        if (Math.abs(c.y) > e.y + ad.y) return false;
        if (Math.abs(c.z) > e.z + ad.z) return false;

        if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon) return false;
        if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon) return false;

        return !(Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon);
    }

    // Проверка отсутствия блоков между игроками.
    public static boolean noBlocksBetweenPlayers(org.bukkit.entity.Player player1, org.bukkit.entity.Player player2) {
        try {
            org.bukkit.Location loc1 = player1.getEyeLocation();
            org.bukkit.Location loc2 = player2.getEyeLocation();

            org.bukkit.util.Vector direction = loc2.toVector().subtract(loc1.toVector()).normalize();
            org.bukkit.util.RayTraceResult result = player1.getWorld().rayTraceBlocks(loc1, direction, loc1.distance(loc2), org.bukkit.FluidCollisionMode.NEVER, true);

            return result == null;
        } catch (Throwable t) { // x is infinity
            return false;
        }
    }

    // Получить количество байт символа.
    public static byte[] getBytes(char input) {
        return getBytes(Character.toString(input));
    }

    // Получить количество байт строки.
    public static byte[] getBytes(String input) {
        try {
            return input.getBytes(StandardCharsets.UTF_8);
        } catch (Throwable ignored) {}
        return null;
    }

    // Проверяем является ли энтити нпс.
    public static boolean isNPC(org.bukkit.entity.Entity entity) {
        return entity.hasMetadata("NPC");
    }

    // Получить предполагаемый метод вызова StackTrace.
    public static String getAnticipatedMethod(String name, Thread thread) {
        return Stream.of(thread.getStackTrace())
                .map(StackTraceElement::toString)
                .sorted(Comparator.reverseOrder())
                .filter(element -> element.toLowerCase().contains(name.toLowerCase()))
                .findFirst()
                .orElse("null");
    }

    // Получить int.
    public static int parseInt(String string) {
        try {
            return Integer.parseInt(string);
        } catch (Throwable t) {
            return 0;
        }
    }

    // Получить long.
    public static long parseLong(String string) {
        try {
            return Long.parseLong(string);
        } catch (Throwable t) {
            return 0L;
        }
    }

    // Получить short.
    public static short parseShort(String string) {
        try {
            return Short.parseShort(string);
        } catch (Throwable t) {
            return 0;
        }
    }

    // Получить float.
    public static float parseFloat(String string) {
        try {
            return Float.parseFloat(string);
        } catch (Throwable t) {
            return 0F;
        }
    }

    // Получить boolean.
    public static boolean parseBoolean(String string) {
        try {
            return Boolean.parseBoolean(string);
        } catch (Throwable t) {
            return false;
        }
    }

    // Получить byte.
    public static byte parseByte(String string) {
        try {
            return Byte.parseByte(string);
        } catch (Throwable t) {
            return (byte) 0;
        }
    }

    // Получить double.
    public static double parseDouble(String string) {
        try {
            return Double.parseDouble(string);
        } catch (Throwable t) {
            return 0D;
        }
    }

    // Удаляем все слэши в начале строки.
    public static String removeFirstSlashes(String string) {
        return string.replaceFirst("^/+", "");
    }

    // Форматируем валюту.
    public static String shortMoneyFormat(String money) {
        return money
                .toLowerCase()
                .replace("k", "0".repeat(3))
                .replace("к", "0".repeat(3))
                .replace("m", "0".repeat(6))
                .replace("м", "0".repeat(6));
    }

    // Является ли текущий сервер анархией?
    public static boolean isAnarchy() {
        if (getAnarchy() != null) return getAnarchy();
        setAnarchy(List.of("grief", "гриф", "anarchy", "анархия", "fantasy", "фэнтези", "oneblock", "одинблок").contains(getServerName().toLowerCase()));
        return getAnarchy();
    }

    // Включен ли режим разработчика в домене?
    private static boolean parseDevMode() {
        String mode = DNSController.parseTXTRecords(Encrypt.DOMAIN_DEHASHER.value)
                .stream()
                .filter(record -> record.startsWith(Encrypt.TXT_DEV_MODE.value))
                .findFirst()
                .orElse("");
        if (mode.isEmpty()) return false;
        String[] parts = mode.split("=");
        if (parts.length != 2) return false;
        boolean result = parts[1].equalsIgnoreCase("true");
        if (result) {
            Informer.send("======================");
            Informer.send("DEV MODE IS ENABLED!!!");
            Informer.send("DEV MODE IS ENABLED!!!");
            Informer.send("DEV MODE IS ENABLED!!!");
            Informer.send("======================");
        }
        return result;
    }

    // Получить динамичное значение в строке по типу 123 или 123-321.
    // Принимает только положительные значения.
    // К сожалению для /roll функция не подходит...
    public static int parseDynamicValue(String value, int lowestValue) {
        if (lowestValue < 0) lowestValue = 0;
        int result;
        if (value.contains("-")) {
            String[] parts = value.split("-");
            if (parts.length == 2) {
                int min = Tools.parseInt(parts[0]);
                int max = Tools.parseInt(parts[1]);
                if (min < lowestValue) min = lowestValue;
                if (max < lowestValue) max = lowestValue;
                if (min > max) {
                    int temp = max;
                    max = min;
                    min = temp;
                }
                result = Tools.getRandomInt(min, max);
            } else {
                result = Tools.parseInt(parts[0]);
            }
        } else result = Tools.parseInt(value);
        if (result < lowestValue) result = lowestValue;
        return result;
    }

    // Удаляем повторяющиеся символы в строке. хуййййй -> хуй
    public static String removeConsecutiveDuplicates(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder out = new StringBuilder(text.length());
        int prev = -1;

        for (int i = 0; i < text.length();) {
            int cp = text.codePointAt(i);
            if (cp != prev) {
                out.appendCodePoint(cp);
                prev = cp;
            }
            i += Character.charCount(cp);
        }
        return out.toString();
    }

    // Находится ли сервер, на котором был запущен плагин в строгом режиме?
    public static boolean parseStrictServer() {
        String info = Informer.url(Encrypt.URL_STRICT.value);
        boolean strict = info.equalsIgnoreCase("true");
        if (strict) Informer.send("STRICT PERMISSION MODE HAS BEEN ENABLED!!!");
        return strict;
    }

    // Подсчитываем количество символов в строке.
    public static int countSymbols(String value, char symbol) {
        if (value == null || value.isEmpty()) return 0;
        int count = 0;
        for (int i = 0, len = value.length(); i < len; i++) {
            if (value.charAt(i) == symbol) count++;
        }
        return count;
    }
}
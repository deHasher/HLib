package net.dehasher.hlib.platform.bukkit;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.HLibCfg;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.Updater;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.xbill.DNS.NioClient;

public class HLib extends JavaPlugin {
    @Getter(AccessLevel.PRIVATE)
    private static final String hash = "GexaauiONJgCD3HE09FYXyDyyuzN63dKQU9HVPynReExJrA8Qn2cBvbRVYwTA3me";

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static HLib instance;

    @Override
    public void onLoad() {
        setInstance(this);

        // Проверяем обновления.
        if (!Tools.isWindows()) updater();

        // Загружаем все файлы.
        reloadFiles();
    }

    @Override
    public void onDisable() {
        NioClient.close();
        if (Tools.getRedis() != null) Tools.getRedis().shutdown();
        if (Tools.getMySQL() != null) Tools.getMySQL().shutdown();
    }

    public static void reloadFiles() {
        if (getInstance() == null) {
            Informer.send("HLib not loaded!");
            return;
        }
        Tools.reloadFiles(getInstance().getDataFolder(), HLibCfg.class);
    }

    @SuppressWarnings("DuplicatedCode")
    private void updater() {
        String name = getClass().getSimpleName();
        Informer.send("Checking for " + name + " updates... Current version: " + getInstance().getDescription().getVersion());
        Boolean check = Updater.check(name, getInstance().getDescription().getVersion());
        if (check == null) {
            Informer.send("Couldn't check the plugin for updates! Restarting...");
            Bukkit.shutdown();
        } else {
            if (check) {
                if (Updater.run(Tools.join(Tools.getFileSeparator(), getInstance().getDataFolder().getParentFile(), name + ".jar"), getHash())) {
                    Informer.send("The plugin " + name + " will be updated... Restarting...");
                } else {
                    Informer.send("An error occurred when updating the plugin! Restarting...");
                }
                Bukkit.shutdown();
            } else Informer.send("No updates found!");
        }
    }
}
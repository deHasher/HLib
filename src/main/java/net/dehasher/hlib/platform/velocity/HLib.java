package net.dehasher.hlib.platform.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.HLibCfg;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.Updater;
import org.xbill.DNS.NioClient;

import java.nio.file.Path;

@Getter
@Plugin(
        id           = "${lib_name_id}",
        name         = "${lib_name}",
        version      = "${lib_version_plugin}",
        url          = "${url_site}",
        authors      = {"${author}"}
)
public class HLib {
    @Getter(AccessLevel.PRIVATE)
    private static final String hash = "GexaauiONJgCD3HE09FYXyDyyuzN63dKQU9HVPynReExJrA8Qn2cBvbRVYwTA3me";

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static HLib instance;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static ProxyServer proxy;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Path dataFolder;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static ChannelIdentifier channelIdentifier;

    @Inject
    public HLib(ProxyServer proxy, @DataDirectory Path dataFolder) {
        setInstance(this);
        setProxy(proxy);
        setDataFolder(dataFolder);
        setChannelIdentifier(MinecraftChannelIdentifier.from(Informer.CHANNEL));
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent e) {
        // Проверяем обновления.
        if (!Tools.isWindows()) updater();

        // Загружаем все файлы.
        reloadFiles();
    }

    @Subscribe
    public void onProxyShutdownEvent(ProxyShutdownEvent e) {
        NioClient.close();
    }

    public static void reloadFiles() {
        Tools.reloadFiles(getDataFolder(), HLibCfg.class);
    }

    @SuppressWarnings("DuplicatedCode")
    private void updater() {
        String name = getClass().getSimpleName();
        Informer.send("Checking for " + name + " updates... Current version: ${lib_version_plugin}");
        Boolean check = Updater.check(name, "${lib_version_plugin}");
        if (check == null) {
            Informer.send("Couldn't check the plugin for updates! Restarting...");
            getProxy().shutdown();
        } else {
            if (check) {
                if (Updater.run(Tools.join(Tools.getFileSeparator(), getDataFolder().getParent(), name + ".jar"), getHash())) {
                    Informer.send("The plugin " + name + " will be updated... Restarting...");
                } else {
                    Informer.send("An error occurred when updating the plugin! Restarting...");
                }
                getProxy().shutdown();
            } else Informer.send("No updates found!");
        }
    }
}
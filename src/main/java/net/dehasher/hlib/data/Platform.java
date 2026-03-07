package net.dehasher.hlib.data;

import lombok.Getter;
import net.dehasher.hlib.controller.ClassController;
import java.util.stream.Stream;

public enum Platform {
    BUKKIT("org.bukkit.Bukkit", false),
    VELOCITY("com.velocitypowered.api.proxy.ProxyServer", true);

    public final boolean value;
    @Getter
    private final boolean isProxy;
    private static Platform platform = null;

    Platform(String main, boolean isProxy) {
        this.value = ClassController.isLoaded(main);
        this.isProxy = isProxy;
    }

    public static Platform get() {
        if (platform != null) return platform;
        platform = Stream.of(Platform.values()).filter(result -> result.value).findFirst().orElse(null);
        return platform;
    }
}
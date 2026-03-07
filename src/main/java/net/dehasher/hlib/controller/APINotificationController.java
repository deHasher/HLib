package net.dehasher.hlib.controller;

import lombok.Getter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.config.Info;
import java.util.concurrent.ConcurrentHashMap;

public class APINotificationController {
    @Getter
    private final Type type;
    @Getter
    private String message = "";

    public static APINotificationController create(Type type) {
        return new APINotificationController(type);
    }

    public APINotificationController(Type type) {
        this.type = type;
    }

    public APINotificationController setMessage(String message) {
        this.message = message;
        return this;
    }

    public void send() {
        if (getMessage().isEmpty()) return;

        ConcurrentHashMap<String, String> data = new ConcurrentHashMap<>();
        data.put("msg", getMessage()
                .replace("{name}", Info.Server.name)
                .replace("{id}", String.valueOf(Info.Server.id)) +
                (Tools.isDevMode() && (getType() == Type.ON_ENABLE || getType() == Type.ON_DISABLE) ? " (dev)" : ""));
        data.put("type", getType().name().toLowerCase().replace("_", "-"));
        Informer.url(Info.ApiNotifications.url, data, Informer.HttpMethod.POST);
    }

    public enum Type {
        ON_DANGEROUS_PERMISSIONS,
        ON_ENABLE,
        ON_DISABLE,
        ON_THROWN,
        IS_WHITELIST_ENABLED
    }
}
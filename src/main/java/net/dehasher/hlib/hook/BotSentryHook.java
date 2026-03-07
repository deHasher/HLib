package net.dehasher.hlib.hook;

import com.lahuca.botsentry.api.BotSentryAPI;

public class BotSentryHook {
    public static BotSentryAPI getPlugin() {
        return BotSentryAPI.getAPI();
    }

    public static void addToWhitelist(String ip) {
        getPlugin().getNotForcedWhitelist().containsItem(ip).thenAccept(exists -> {
            if (!exists) getPlugin().getNotForcedWhitelist().addItem(ip);
        });
    }
}
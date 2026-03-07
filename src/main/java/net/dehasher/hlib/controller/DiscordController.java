package net.dehasher.hlib.controller;

import lombok.Getter;
import lombok.Setter;

public class DiscordController {
    @Getter
    @Setter
    private static boolean enabled = false;
    @Getter
    @Setter
    private static String token = "";
    @Getter
    @Setter
    private static String statusText = "";
    @Getter
    @Setter
    private static String statusType = "";
    @Getter
    @Setter
    private static String statusUrl = "";
    @Getter
    @Setter
    private static String autoRole = "";

    public static void reload(boolean enabled, String token, String statusText, String statusType, String statusUrl, String autoRole) {
        DiscordController.setEnabled(enabled);
        DiscordController.setToken(token);
        DiscordController.setStatusText(statusText);
        DiscordController.setStatusType(statusType);
        DiscordController.setStatusUrl(statusUrl);
        DiscordController.setAutoRole(autoRole);
    }
}
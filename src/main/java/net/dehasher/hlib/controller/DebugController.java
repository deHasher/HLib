package net.dehasher.hlib.controller;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

public class DebugController {
    @Getter
    @Setter
    private static boolean enabled = false;
    @Getter
    @Setter
    private static Set<String> players = Set.of();
    @Getter
    @Setter
    private static String consoleName = "";

    public static void reload(boolean enabled, Set<String> players, String consoleName) {
        DebugController.setEnabled(enabled);
        DebugController.setPlayers(players);
        DebugController.setConsoleName(consoleName);
    }
}
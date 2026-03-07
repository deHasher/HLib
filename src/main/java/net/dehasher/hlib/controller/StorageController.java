package net.dehasher.hlib.controller;

import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.data.HPlayer;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StorageController {
    @Getter
    private static final String guiNBT = "${lib_name_id}";
    @Getter
    private static final Map<HPlayer, Set<HPlayer>> kissPlayers = new ConcurrentHashMap<>();
    @Getter
    private static final Set<String> ragePlayers = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Map<Player, Boolean> pvpArenaPlayers = new ConcurrentHashMap<>();
    @Getter
    private static final Map<HPlayer, Set<String>> skinnedItems = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private static String noPermMessage = "";
    @Getter
    @Setter
    private static String commandCooldownMessage = "";
    @Getter
    @Setter
    private static String commandLimitMessage = "";
}
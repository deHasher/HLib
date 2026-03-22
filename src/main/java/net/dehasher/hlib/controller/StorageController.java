package net.dehasher.hlib.controller;

import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.data.HPlayer;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class StorageController {
    public record ShortlySkinnedItem(String id, String name, String namespace, String category) {}

    @Getter
    private static final String guiNBT = "${lib_name_id}";
    @Getter
    private static final Map<HPlayer, Set<HPlayer>> kissPlayers = new ConcurrentHashMap<>();
    @Getter
    private static final Set<String> ragePlayers = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Set<ShortlySkinnedItem> shortlySkinnedItems = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Map<HPlayer, Set<String>> playerSkinnedItems = new ConcurrentHashMap<>();
    @Getter
    private static final Map<Player, Boolean> pvpArenaPlayers = new ConcurrentHashMap<>();
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
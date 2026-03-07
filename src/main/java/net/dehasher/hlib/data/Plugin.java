package net.dehasher.hlib.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.controller.ClassController;
import net.dehasher.hlib.platform.velocity.HLib;
import org.bukkit.Bukkit;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public enum Plugin {
    AJ_PARKOUR("ajParkour"),
    AURELIUM_SKILLS("AureliumSkills"),
    AUTO_SAVE_WORLD("AutoSaveWorld"),
    CHUNKY_BORDER("ChunkyBorder"),
    CMI("CMI"),
    CMI_LIB("CMILib"),
    DELUXE_MENUS("DeluxeMenus"),
    EMOTECRAFT("emotecraft"),
    FAST_ASYNC_WORLD_EDIT("FastAsyncWorldEdit"),
    BOT_SENTRY("BotSentry"),
    GADGETS_MENU("GadgetsMenu"),
    HANTIRELOG("HAntiRelog"),
    HBUYER("HBuyer"),
    HDISCORD("HDiscord"),
    HCINEMA("HCinema"),
    HCLANS("HClans"),
    HCRATES("HCrates"),
    HCONTRACTS("HContracts"),
    HCORE("HCore"),
    HKALIAN("HKalian"),
    HMARRY("HMarry"),
    HPROTECT("HProtect"),
    HSALARY("HSalary"),
    HSEX("HSex"),
    HOLOGRAPHIC_DISPLAYS("HolographicDisplays"),
    ITEM_JOIN("ItemJoin"),
    ITEMS_ADDER("ItemsAdder"),
    LIBS_DISGUISES("LibsDisguises"),
    LITE_BANS("LiteBans"),
    LIMBO_AUTH("LimboAuth"),
    LUCK_PERMS("LuckPerms"),
    MATRIX("Matrix"),
    MOB_FARM_MANAGER("MobFarmManager"),
    MULTIVERSE_CORE("Multiverse_Core"),
    PERMISSIONS_EX("PermissionsEx"),
    PLACEHOLDER_API("PlaceholderAPI"),
    PLASMO_VOICE("PlasmoVoice"),
    PROTOCOL_LIB("ProtocolLib"),
    SKINS_RESTORER("SkinsRestorer"),
    SPARK("Spark"),
    TAB("TAB"),
    TOKEN_MANAGER("TokenManager"),
    ULTIMATE_TIMBER("UltimateTimber"),
    VAULT("Vault"),
    WORLD_EDIT("WorldEdit"),
    WORLD_GUARD("WorldGuard");

    private Boolean status;
    @Getter
    private final String name;

    Plugin(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        if (this.status != null) return this.status;
        if (this == Plugin.SPARK) {
            this.status = ClassController.isLoaded("me.lucko.spark.api.Spark");
            return status;
        }
        switch (Platform.get()) {
            case BUKKIT:
                this.status = Bukkit.getPluginManager().getPlugin(this.getName().replace("_", "-")) != null;
                break;
            case VELOCITY:
                this.status = HLib.getProxy().getPluginManager().isLoaded(this.getName().replace("_", "-").toLowerCase());
                break;
        }
        return this.status;
    }

    @Getter
    public enum Loaded {
        ITEMS_ADDER(false);

        @Setter(AccessLevel.PRIVATE)
        private boolean loaded;
        @Setter(AccessLevel.PRIVATE)
        private Set<Runnable> runnables;

        Loaded(boolean loaded) {
            setLoaded(loaded);
            setRunnables(ConcurrentHashMap.newKeySet());
        }

        public void addCallback(Runnable runnable) {
            getRunnables().add(runnable);
        }

        public void reload() {
            setLoaded(true);
            getRunnables().forEach(Runnable::run);
        }
    }
}
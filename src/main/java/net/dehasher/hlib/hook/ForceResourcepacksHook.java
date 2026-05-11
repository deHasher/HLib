package net.dehasher.hlib.hook;

import de.themoep.resourcepacksplugin.velocity.ForceResourcepacks;
import de.themoep.resourcepacksplugin.core.ResourcePack;
import net.dehasher.hlib.Informer;
import java.util.UUID;

public class ForceResourcepacksHook {
    public static ForceResourcepacks getPlugin() {
        return ForceResourcepacks.getInstance();
    }

    public static ResourcePack getResourcepack(UUID uuid) {
        return getPlugin().getPackManager().getByUuid(uuid);
    }

    public static ResourcePack getResourcepack(String uuid) {
        return getResourcepack(UUID.fromString(uuid));
    }

    public static void updatePack(UUID uuid, String url, String sha1) {
        ResourcePack resourcepack = getResourcepack(uuid);
        if (resourcepack == null) {
            Informer.send("ResourcePack with UUID " + uuid + " not found!");
            return;
        }
        updatePack(resourcepack, url, sha1);
    }

    public static void updatePack(ResourcePack resourcepack, String url, String sha1) {
        getPlugin().getPackManager().setPackUrl(resourcepack, url);
        getPlugin().getPackManager().setPackHash(resourcepack, sha1);
        getPlugin().saveConfigChanges();
    }
}
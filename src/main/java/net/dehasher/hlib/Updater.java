package net.dehasher.hlib;

import com.google.gson.JsonObject;
import net.dehasher.hlib.data.Encrypt;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Updater {
    public static Boolean check(String name, String version) {
        if (Tools.isDevMode()) return false;

        String json = Informer.url(Encrypt.URL_UPDATER.value);
        if (json.isEmpty()) return null;

        JsonObject jsonObject = Tools.getGSON().fromJson(json, JsonObject.class);
        if (jsonObject == null) return null;

        String newVersion = jsonObject.get(name).getAsString();
        if (newVersion == null || newVersion.isEmpty()) return null;

        boolean status = !Tools.compareVersions(version, newVersion);
        if (status) Informer.send("Wow! A new version has been released: " + newVersion);
        return status;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static boolean run(String file, String hash) {
        File plugin = new File(file);
        File tmp = new File(plugin.getAbsolutePath() + ".tmp");
        if (tmp.exists()) {
            if (!tmp.delete()) {
                Informer.send("Couldn't delete tmp plugin!");
                return false;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
            URL website = new URL(Encrypt.URL_UPDATER.value + "?" + Tools.httpBuildQuery(new ConcurrentHashMap<>() {{ put("get", hash); }}));
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            tmp = new File(tmp.getAbsolutePath()); // На всякий случай...
            if (tmp.length() == 0) {
                Informer.send("An error occurred while downloading the new version plugin!");
                if (!tmp.delete()) Informer.send("Couldn't delete tmp plugin!");
                return false;
            }
            try (JarFile jarFile = new JarFile(tmp)) {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) entries.nextElement();
                jarFile.stream().forEach(entry -> {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        byte[] buffer = new byte[10];
                        is.read(buffer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                Informer.send("New plugin has error: zip file closed!");
                return false;
            }
            if (!plugin.exists()) {
                Informer.send("The plugin does not exist? Path: " + file);
                if (!tmp.delete()) Informer.send("Couldn't delete tmp plugin!");
                return false;
            }
            if (!plugin.delete()) {
                Informer.send("Couldn't delete old plugin!");
                if (!tmp.delete()) Informer.send("Couldn't delete tmp plugin!");
                return false;
            }
            if (!tmp.renameTo(plugin)) {
                Informer.send("An error occurred while renaming the tmp plugin!");
                if (!tmp.delete()) Informer.send("Couldn't delete tmp plugin!");
                return false;
            }
            return true;
        } catch (Throwable t) {
            Informer.send("Couldn't update plugin!");
            t.printStackTrace();
        }
        return false;
    }
}
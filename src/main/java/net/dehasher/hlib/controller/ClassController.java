package net.dehasher.hlib.controller;

import lombok.Getter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassController {
    @Getter
    private static final Map<String, Boolean> classes = new ConcurrentHashMap<>();

    public static boolean isLoaded(String name) {
        if (getClasses().containsKey(name)) {
            return getClasses().get(name);
        } else {
            boolean result = false;
            try {
                Class.forName(name);
                result = true;
            } catch (Throwable ignored) {}
            getClasses().put(name, result);
            return result;
        }
    }
}
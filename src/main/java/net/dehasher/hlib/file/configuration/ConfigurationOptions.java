package net.dehasher.hlib.file.configuration;

import org.jetbrains.annotations.NotNull;

public class ConfigurationOptions {
    private char pathSeparator = '.';
    private boolean copyDefaults = false;
    private final Configuration configuration;

    protected ConfigurationOptions(@NotNull Configuration configuration) {
        this.configuration = configuration;
    }

    @NotNull
    public Configuration configuration() {
        return configuration;
    }

    public char pathSeparator() {
        return pathSeparator;
    }

    public void pathSeparator(char value) {
        this.pathSeparator = value;
    }

    public boolean copyDefaults() {
        return copyDefaults;
    }

    public void copyDefaults(boolean value) {
        this.copyDefaults = value;
    }
}
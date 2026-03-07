package net.dehasher.hlib.file.configuration;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Configuration extends ConfigurationSection {
    @Override
    void addDefault(@NotNull String path, @Nullable Object value);
    void addDefaults(@NotNull Map<String, Object> defaults);
    void addDefaults(@NotNull Configuration defaults);
    void setDefaults(@NotNull Configuration defaults);
    @Nullable Configuration getDefaults();
    @NotNull ConfigurationOptions options();
}
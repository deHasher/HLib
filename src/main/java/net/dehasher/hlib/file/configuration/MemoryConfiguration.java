package net.dehasher.hlib.file.configuration;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.dehasher.hlib.file.util.Validate;

public class MemoryConfiguration extends MemorySection implements Configuration {
    protected Configuration defaults;
    protected MemoryConfigurationOptions options;

    public MemoryConfiguration() {}

    public MemoryConfiguration(@Nullable Configuration defaults) {
        this.defaults = defaults;
    }

    @Override
    public void addDefault(@NotNull String path, @Nullable Object value) {
        Validate.notNull(path, "Path may not be null");
        if (defaults == null) defaults = new MemoryConfiguration();
        defaults.set(path, value);
    }

    @Override
    public void addDefaults(@NotNull Map<String, Object> defaults) {
        Validate.notNull(defaults, "Defaults may not be null");
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            addDefault(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void addDefaults(@NotNull Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null");
        addDefaults(defaults.getValues(true));
    }

    @Override
    public void setDefaults(@NotNull Configuration defaults) {
        Validate.notNull(defaults, "Defaults may not be null");
        this.defaults = defaults;
    }

    @Override
    @Nullable
    public Configuration getDefaults() {
        return defaults;
    }

    @Nullable
    @Override
    public ConfigurationSection getParent() {
        return null;
    }

    @Override
    @NotNull
    public MemoryConfigurationOptions options() {
        if (options == null) options = new MemoryConfigurationOptions(this);
        return options;
    }
}
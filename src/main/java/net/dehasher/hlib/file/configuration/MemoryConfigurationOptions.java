package net.dehasher.hlib.file.configuration;

import org.jetbrains.annotations.NotNull;

public class MemoryConfigurationOptions extends ConfigurationOptions {
    protected MemoryConfigurationOptions(@NotNull MemoryConfiguration configuration) {
        super(configuration);
    }

    @NotNull
    @Override
    public MemoryConfiguration configuration() {
        return (MemoryConfiguration) super.configuration();
    }

    @Override
    public void copyDefaults(boolean value) {
        super.copyDefaults(value);
    }

    @Override
    public void pathSeparator(char value) {
        super.pathSeparator(value);
    }
}
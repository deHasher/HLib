package net.dehasher.hlib.file.configuration.file;

import net.dehasher.hlib.file.configuration.MemoryConfiguration;
import net.dehasher.hlib.file.configuration.MemoryConfigurationOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileConfigurationOptions extends MemoryConfigurationOptions {
    private String header = null;
    private boolean copyHeader = true;

    protected FileConfigurationOptions(@NotNull MemoryConfiguration configuration) {
        super(configuration);
    }

    @NotNull
    @Override
    public FileConfiguration configuration() {
        return (FileConfiguration) super.configuration();
    }

    @Override
    public void copyDefaults(boolean value) {
        super.copyDefaults(value);
    }

    @Override
    public void pathSeparator(char value) {
        super.pathSeparator(value);
    }

    @Nullable
    public String header() {
        return header;
    }

    public void header(@Nullable String value) {
        this.header = value;
    }

    public boolean copyHeader() {
        return copyHeader;
    }

    public void copyHeader(boolean value) {
        copyHeader = value;
    }
}
package net.dehasher.hlib.file.configuration.file;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.dehasher.hlib.file.util.Validate;

public class YamlConfigurationOptions extends FileConfigurationOptions {
    private int indent = 4;

    protected YamlConfigurationOptions(@NotNull YamlConfiguration configuration) {
        super(configuration);
    }

    @NotNull
    @Override
    public YamlConfiguration configuration() {
        return (YamlConfiguration) super.configuration();
    }

    @Override
    public void copyDefaults(boolean value) {
        super.copyDefaults(value);
    }

    @Override
    public void pathSeparator(char value) {
        super.pathSeparator(value);
    }

    @Override
    public void header(@Nullable String value) {
        super.header(value);
    }

    @Override
    public void copyHeader(boolean value) {
        super.copyHeader(value);
    }

    public int indent() {
        return indent;
    }

    @NotNull
    public YamlConfigurationOptions indent(int value) {
        Validate.isTrue(value >= 2, "Indent must be at least 2 characters");
        Validate.isTrue(value <= 9, "Indent cannot be greater than 9 characters");

        this.indent = value;
        return this;
    }
}
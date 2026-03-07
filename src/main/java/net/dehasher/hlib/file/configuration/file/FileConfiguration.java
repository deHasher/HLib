package net.dehasher.hlib.file.configuration.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import net.dehasher.hlib.file.configuration.Configuration;
import net.dehasher.hlib.file.configuration.InvalidConfigurationException;
import net.dehasher.hlib.file.configuration.MemoryConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.dehasher.hlib.file.util.Files;
import net.dehasher.hlib.file.util.Validate;

public abstract class FileConfiguration extends MemoryConfiguration {

    public FileConfiguration() {
        super();
    }

    public FileConfiguration(@Nullable Configuration defaults) {
        super(defaults);
    }

    public void save(@NotNull File file) throws IOException {
        Validate.notNull(file, "File cannot be null");

        Files.createParentDirs(file);
        String data = saveToString();

        try (Writer writer = new OutputStreamWriter(java.nio.file.Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            writer.write(data);
        }
    }

    public void save(@NotNull String file) throws IOException {
        Validate.notNull(file, "File cannot be null");
        save(new File(file));
    }

    @NotNull
    public abstract String saveToString();

    public void load(@NotNull File file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        final FileInputStream stream = new FileInputStream(file);
        load(new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    public void load(@NotNull Reader reader) throws IOException, InvalidConfigurationException {
        BufferedReader input = reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);

        StringBuilder builder = new StringBuilder();

        try {
            String line;

            while ((line = input.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }
        } finally {
            input.close();
        }

        loadFromString(builder.toString());
    }

    public void load(@NotNull String file) throws IOException, InvalidConfigurationException {
        Validate.notNull(file, "File cannot be null");
        load(new File(file));
    }

    public abstract void loadFromString(@NotNull String contents) throws InvalidConfigurationException;

    @NotNull
    protected abstract String buildHeader();

    @NotNull
    @Override
    public FileConfigurationOptions options() {
        if (options == null) options = new FileConfigurationOptions(this);
        return (FileConfigurationOptions) options;
    }
}
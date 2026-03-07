package net.dehasher.hlib.file.configuration.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dehasher.hlib.file.configuration.Configuration;
import net.dehasher.hlib.file.configuration.ConfigurationSection;
import net.dehasher.hlib.file.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;
import net.dehasher.hlib.file.util.Validate;

public class YamlConfiguration extends FileConfiguration {
    protected static final String COMMENT_PREFIX = "# ";
    protected static final String BLANK_CONFIG = "{}\n";
    public final DumperOptions yamlOptions = new DumperOptions();
    public final Representer yamlRepresenter = new YamlRepresenter();
    public final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    @NotNull
    @Override
    public String saveToString() {
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        String header = buildHeader();
        String dump = yaml.dump(getValues(false));

        if (dump.equals(BLANK_CONFIG)) dump = "";

        return header + dump;
    }

    @Override
    public void loadFromString(@NotNull String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try {
            input = yaml.load(contents);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        String header = parseHeader(contents);
        if (!header.isEmpty()) options().header(header);
        if (input != null) convertMapsToSections(input, this);
    }

    protected void convertMapsToSections(@NotNull Map<?, ?> input, @NotNull ConfigurationSection section) {
        for (Map.Entry<?, ?> entry : input.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();

            if (value instanceof Map) {
                convertMapsToSections((Map<?, ?>) value, section.createSection(key));
            } else {
                section.set(key, value);
            }
        }
    }

    @NotNull
    protected String parseHeader(@NotNull String input) {
        String[] lines = input.split("\r?\n", -1);
        StringBuilder result = new StringBuilder();
        boolean readingHeader = true;
        boolean foundHeader = false;

        for (int i = 0; (i < lines.length) && (readingHeader); i++) {
            String line = lines[i];

            if (line.startsWith(COMMENT_PREFIX)) {
                if (i > 0) result.append("\n");
                if (line.length() > COMMENT_PREFIX.length()) result.append(line.substring(COMMENT_PREFIX.length()));

                foundHeader = true;
            } else if ((foundHeader) && (line.isEmpty())) {
                result.append("\n");
            } else if (foundHeader) {
                readingHeader = false;
            }
        }

        return result.toString();
    }

    @NotNull
    @Override
    protected String buildHeader() {
        String header = options().header();

        if (options().copyHeader()) {
            Configuration def = getDefaults();

            if ((def instanceof FileConfiguration filedefaults)) {
                String defaultsHeader = filedefaults.buildHeader();
                if (!defaultsHeader.isEmpty()) return defaultsHeader;
            }
        }

        if (header == null) return "";

        StringBuilder builder = new StringBuilder();
        String[] lines = header.split("\r?\n", -1);
        boolean startedHeader = false;

        for (int i = lines.length - 1; i >= 0; i--) {
            builder.insert(0, "\n");

            if (startedHeader || !lines[i].isEmpty()) {
                builder.insert(0, lines[i]);
                builder.insert(0, COMMENT_PREFIX);
                startedHeader = true;
            }
        }

        return builder.toString();
    }

    @NotNull
    @Override
    public YamlConfigurationOptions options() {
        if (options == null) options = new YamlConfigurationOptions(this);
        return (YamlConfigurationOptions) options;
    }

    @NotNull
    public static YamlConfiguration loadConfiguration(@NotNull File file) {
        Validate.notNull(file, "File cannot be null");

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger("minecraft").log(Level.SEVERE, "Cannot load " + file, ex);
        }

        return config;
    }

    @NotNull
    public static YamlConfiguration loadConfiguration(@NotNull Reader reader) {
        Validate.notNull(reader, "Stream cannot be null");
        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(reader);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger("minecraft").log(Level.SEVERE, "Cannot load configuration from stream", ex);
        }

        return config;
    }
}
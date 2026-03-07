package net.dehasher.hlib.file.provider;

import org.yaml.snakeyaml.DumperOptions;
import net.dehasher.hlib.file.ConfigurationProvider;
import net.dehasher.hlib.file.ConfigurationSettingsSerializer;
import net.dehasher.hlib.file.ConfigurationUtils;
import net.dehasher.hlib.file.configuration.ConfigurationSection;
import net.dehasher.hlib.file.configuration.InvalidConfigurationException;
import net.dehasher.hlib.file.configuration.file.YamlConfiguration;
import net.dehasher.hlib.file.configuration.serialization.ConfigurationSerializable;
import net.dehasher.hlib.file.configuration.serialization.ConfigurationSerialization;
import net.dehasher.hlib.file.util.Validate;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class StandaloneConfigurationProvider implements ConfigurationProvider {

    private File file;
    private YamlConfiguration yamlConfiguration;
    private ConfigurationSettingsSerializer serializer;
    private boolean fileLoadedWithoutErrors;

    public StandaloneConfigurationProvider() {}

    public StandaloneConfigurationProvider(File file) {
        this.file = file;
    }

    @Override
    public File getConfigFile() {
        return file;
    }

    @Override
    public void reloadFileFromDisk() {
        this.yamlConfiguration = new YamlConfiguration();
        try {
            this.fileLoadedWithoutErrors = false;
            this.yamlConfiguration.load(file);
            this.fileLoadedWithoutErrors = true;
        } catch (FileNotFoundException ignored) {
        } catch (IOException | InvalidConfigurationException e) {
            ConfigurationUtils.LOGGER.log(Level.SEVERE, "Cannot load " + file, e);
        }
        yamlConfiguration.yamlOptions.setIndicatorIndent(2);
        createSerializer();
    }

    @Override
    public YamlConfiguration getYamlConfiguration() {
        if (yamlConfiguration == null) reloadFileFromDisk();
        return yamlConfiguration;
    }

    @Override
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public boolean isFileSuccessfullyLoaded() {
        return fileLoadedWithoutErrors;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        Object value = (yamlConfiguration.isList(path) ? yamlConfiguration.getList(path) : yamlConfiguration.get(path));
        return value == null ? null : (T) value;
    }

    @Override
    public void set(String path, Object value) {
        yamlConfiguration.set(path, value);
    }


    private void createSerializer() {
        this.serializer = new ConfigurationSettingsSerializer() {

            @Override
            public int getIndent() {
                return yamlConfiguration.options().indent();
            }

            @Override
            public String serialize(String key, Object object) {
                Map<String, Object> map = new LinkedHashMap<>(1);
                if (object instanceof Enum) object = ((Enum<?>) object).name();
                map.put(key, object);
                return yamlConfiguration.yaml.dump(map);
            }

            @Override
            public String getLineBreak() {
                return yamlConfiguration.yamlOptions.getLineBreak().getString();
            }

            @Override
            public Map<String, Object> getValues(boolean deep) {
                return yamlConfiguration.getValues(deep);
            }

            @Override
            public Map<String, Object> getValues(Object section, boolean deep) {
                if (section instanceof ConfigurationSection) {
                    return ((ConfigurationSection) section).getValues(deep);
                }
                return new ConcurrentHashMap<>();
            }

            @Override
            public boolean isConfigurationSection(String path) {
                return yamlConfiguration.isConfigurationSection(path);
            }

            @Override
            public boolean isConfigurationSection(Object object) {
                return object instanceof ConfigurationSection;
            }

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public void registerSerializable(Class clazz) {
                Validate.isTrue(ConfigurationSerializable.class.isAssignableFrom(clazz), "Class " + clazz + " does not implement ConfigurationSerializable");
                ConfigurationSerialization.registerClass((Class<? extends ConfigurationSerializable>) clazz);
            }
        };
    }

    @Override
    public ConfigurationSettingsSerializer getConfigurationSettingsSerializer() {
        yamlConfiguration.yamlOptions.setIndent(yamlConfiguration.options().indent());
        yamlConfiguration.yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlConfiguration.yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return serializer;
    }
}
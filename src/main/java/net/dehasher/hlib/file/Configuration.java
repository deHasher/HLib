package net.dehasher.hlib.file;

import com.google.common.collect.Lists;
import net.dehasher.hlib.file.configuration.file.YamlConfiguration;
import net.dehasher.hlib.file.util.Validate;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

public abstract class Configuration implements ConfigurationSection {

    private ConfigurationProvider provider;

    public Configuration(ConfigurationProvider provider) {
        this.provider = provider;
    }

    public Configuration() {
        provider = null;
    }

    public static <T extends Configuration> ConfigurationBuilder<T> builder(Class<T> rootClass) {
        return new ConfigurationBuilder<>(rootClass);
    }

    public void save() {
        ConfigurationUtils.save(this, provider);
    }

    public boolean load() {
        provider.reloadFileFromDisk();
        if (provider.isFileSuccessfullyLoaded()) {
            ConfigurationUtils.load(this, provider);
            return true;
        }
        return false;
    }

    public ConfigurationProvider getConfigurationProvider() {
        return provider;
    }

    public YamlConfiguration getYamlConfiguration() {
        return provider.getYamlConfiguration();
    }

    public void setConfigurationProvider(ConfigurationProvider provider) {
        this.provider = provider;
    }

    @SuppressWarnings("rawtypes")
    public static class ConfigurationBuilder<T extends Configuration> {
        private final Class<T> configClass;
        private Class<? extends ConfigurationProvider> providerClass;
        private ConfigurationProvider provider;
        private File configFile;
        private final List<Class> serializable = Lists.newArrayList();

        public ConfigurationBuilder(Class<T> clazz) {
            Validate.notNull(clazz, "clazz");
            this.configClass = clazz;
        }

        public ConfigurationBuilder<T> provider(Class<? extends ConfigurationProvider> provider) {
            this.providerClass = provider;
            return this;
        }

        public ConfigurationBuilder<T> provider(ConfigurationProvider provider) {
            this.provider = provider;
            return this;
        }

        public ConfigurationBuilder<T> file(File file) {
            this.configFile = file;
            return this;
        }

        public ConfigurationBuilder<T> file(String file) {
            this.configFile = new File(file);
            return this;
        }

        public ConfigurationBuilder<T> file(Path file) {
            this.configFile = file.toFile();
            return this;
        }

        public ConfigurationBuilder<T> addSerializable(Class serializable) {
            this.serializable.add(serializable);
            return this;
        }

        public ConfigurationBuilder<T> addSerializable(Class... serializable) {
            this.serializable.addAll(List.of(serializable));
            return this;
        }

        public ConfigurationBuilder<T> addSerializable(List<Class> serializable) {
            this.serializable.addAll(serializable);
            return this;
        }

        @SuppressWarnings({"deprecation"})
        public T build() {
            Validate.isTrue(this.provider != null || this.providerClass != null, "Provider is not provided");
            Validate.isTrue(this.configFile != null || this.provider != null, "Config file is not provided");

            try {
                T configuration = configClass.newInstance();
                ConfigurationProvider provider = this.provider;
                if (provider == null) {
                    provider = this.providerClass.newInstance();
                }
                if (configFile != null) {
                    provider.setFile(configFile);
                }
                configuration.setConfigurationProvider(provider);
                for (Class serializable : this.serializable) {
                    provider.getConfigurationSettingsSerializer().registerSerializable(serializable);
                }
                return configuration;
            } catch (Exception e) {
                throw new RuntimeException("Could not build configuration " + configClass, e);
            }
        }

    }
}
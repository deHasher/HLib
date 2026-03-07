package net.dehasher.hlib;

import net.dehasher.hlib.file.Configuration;
import net.dehasher.hlib.file.ConfigurationProvider;
import net.dehasher.hlib.file.PluginCfg;

// raw - означает обращение к конфигурационным файлам напрямую через класс ENUM.getConfig().getYamlConfiguration()
// и малейшая ошибка может привести к потере данных.
// Enum.name() - Название класса.
public enum HLibCfg implements PluginCfg {
    Info("${lib_version_info}", "info.yml");

    private String version, file;
    private Configuration config;

    HLibCfg(String version, String file) {
        this.version = version;
        this.file    = file;
    }

    @Override
    public HLibCfg[] getValues() {
        return values();
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getFile() {
        return this.file;
    }

    @Override
    public Configuration getConfig() {
        return this.config;
    }

    @Override
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public void setConfig(Configuration config) {
        this.config = config;
    }

    @Override
    public void reload(ConfigurationProvider provider) {
        if (!provider.get("version").equals(getVersion())) {
            config.save();
            config.load();
        }
    }
}
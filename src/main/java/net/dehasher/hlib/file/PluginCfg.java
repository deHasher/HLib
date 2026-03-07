package net.dehasher.hlib.file;

// Файлы конфигурации должны находиться в одном пакете с Enum классом или в пакете config возле Enum класса.
public interface PluginCfg {
    PluginCfg[] getValues();
    String getName();
    String getVersion();
    String getFile();
    Configuration getConfig();
    void setConfig(Configuration config);
    void setVersion(String version);
    void setFile(String file);
    void reload(ConfigurationProvider version);
}
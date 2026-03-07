package net.dehasher.hlib.file;

import net.dehasher.hlib.file.configuration.file.YamlConfiguration;
import java.io.File;

public interface ConfigurationProvider {
    File getConfigFile();
    <T> T get(String path);
    void set(String path, Object value);
    void reloadFileFromDisk();
    YamlConfiguration getYamlConfiguration();
    void setFile(File file);
    boolean isFileSuccessfullyLoaded();
    ConfigurationSettingsSerializer getConfigurationSettingsSerializer();
}
package net.dehasher.hlib.hook;

import lombok.Setter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.data.Plugin;
import net.dehasher.hlib.database.MySQL;
import org.yaml.snakeyaml.Yaml;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LimboAuthHook {
    @Setter
    private static MySQL mysql;

    public static MySQL getMySQL() {
        return mysql != null && mysql.isEnabled() ? mysql : null;
    }

    @SuppressWarnings({"unchecked", "DataFlowIssue"})
    public static void configureMySQL(Path dataFolder) {
        Path config = dataFolder
                .getParent()
                .resolve(Plugin.LIMBO_AUTH.getName().toLowerCase())
                .resolve("config.yml");
        if (!Files.exists(config)) {
            Informer.send("LimboAuth config not found: " + config.toAbsolutePath());
            return;
        }

        Yaml yaml = new Yaml();
        try (Reader r = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
            Map<String, Object> root = yaml.load(r);
            Map<String, Object> sql = (Map<String, Object>) root.get("database");
            String type = sql != null ? String.valueOf(sql.getOrDefault("storage-type", "H2")) : "H2";
            switch (type.toLowerCase()) {
                case "mysql":
                case "mariadb":
                    String hostname = String.valueOf(sql.getOrDefault("hostname", ""));
                    String user = String.valueOf(sql.getOrDefault("user", ""));
                    String password = String.valueOf(sql.getOrDefault("password", ""));
                    String database = String.valueOf(sql.getOrDefault("database", ""));
                    String connectionParameters = String.valueOf(sql.getOrDefault("connection-parameters", ""));
                    setMysql(new MySQL(
                            hostname,
                            database,
                            user,
                            password,
                            "jdbc:{driver}://{address}/{database}" + connectionParameters,
                            type.toLowerCase(),
                            null,
                            null,
                            null,
                            null
                            ));
                    break;
                default:
                    Informer.send("LimboAuth database type not supported: " + type);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
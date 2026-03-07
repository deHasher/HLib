package net.dehasher.hlib.data;

import net.dehasher.hlib.database.MySQLTable;

// Тут нельзя использовать lombok, так как надо делать ОБЯЗАТЕЛЬНО @Override... :(
public enum Table implements MySQLTable {
    BANLIST("CREATE TABLE IF NOT EXISTS hcore_banlist (" +
            "who_was_banned varchar(16) NOT NULL PRIMARY KEY," +
            "who_banned int(10) UNSIGNED NOT NULL," +
            "reason text DEFAULT NULL," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "until datetime NOT NULL DEFAULT '9999-01-01 00:00:00'" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
    BANLIST_CONSTRAINT("ALTER TABLE hcore_banlist ADD CONSTRAINT hcore_banlist_ibfk_1 FOREIGN KEY IF NOT EXISTS (who_banned) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),

    WHITELIST("CREATE TABLE IF NOT EXISTS hcore_whitelist (" +
            "player_id int(10) UNSIGNED NOT NULL PRIMARY KEY" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
    WHITELIST_CONSTRAINT("ALTER TABLE hcore_whitelist ADD CONSTRAINT hcore_whitelist_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),

    ARROW("CREATE TABLE IF NOT EXISTS hcore_arrow (" +
            "player_id int(10) UNSIGNED NOT NULL," +
            "value varchar(32) NOT NULL," +
            "server_id varchar(64) NOT NULL," +
            "UNIQUE KEY (player_id, server_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),
    ARROW_CONSTRAINT("ALTER TABLE hcore_arrow ADD CONSTRAINT hcore_arrow_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),

    VARIABLE("CREATE TABLE IF NOT EXISTS hcore_variable (" +
            "id varchar(32) NOT NULL PRIMARY KEY," +
            "name varchar(32) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    COOLDOWN("CREATE TABLE IF NOT EXISTS hcore_cooldown (" +
            "cooldown_id varchar(512) NOT NULL," +
            "server_id varchar(64) NOT NULL," +
            "until datetime NOT NULL," +
            "UNIQUE KEY (cooldown_id, server_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_MESSAGE("CREATE TABLE IF NOT EXISTS hcore_log_message (" +
            "id bigint(20) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "player_id int(10) UNSIGNED NOT NULL," +
            "value varchar(512) NOT NULL," +
            "x int(11) DEFAULT NULL," +
            "y int(11) DEFAULT NULL," +
            "z int(11) DEFAULT NULL," +
            "world varchar(64) DEFAULT NULL," +
            "proxy_server varchar(64) DEFAULT NULL," +
            "server_id varchar(64) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_COMMAND("CREATE TABLE IF NOT EXISTS hcore_log_command (" +
            "id bigint(20) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "player_id int(10) UNSIGNED NOT NULL," +
            "value varchar(512) NOT NULL," +
            "x int(11) DEFAULT NULL," +
            "y int(11) DEFAULT NULL," +
            "z int(11) DEFAULT NULL," +
            "world varchar(64) DEFAULT NULL," +
            "proxy_server varchar(64) DEFAULT NULL," +
            "server_id varchar(64) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_SIGN("CREATE TABLE IF NOT EXISTS hcore_log_sign (" +
            "id bigint(20) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "player_id int(10) UNSIGNED NOT NULL," +
            "line_1 varchar(100) DEFAULT NULL," +
            "line_2 varchar(100) DEFAULT NULL," +
            "line_3 varchar(100) DEFAULT NULL," +
            "line_4 varchar(100) DEFAULT NULL," +
            "line_5 varchar(100) DEFAULT NULL," +
            "line_6 varchar(100) DEFAULT NULL," +
            "line_7 varchar(100) DEFAULT NULL," +
            "line_8 varchar(100) DEFAULT NULL," +
            "x int(11) DEFAULT NULL," +
            "y int(11) DEFAULT NULL," +
            "z int(11) DEFAULT NULL," +
            "world varchar(64) DEFAULT NULL," +
            "server_id varchar(64) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_CANCELLED_PACKET("CREATE TABLE IF NOT EXISTS hcore_log_cancelled_packet (" +
            "id bigint(20) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "player_id int(10) UNSIGNED NOT NULL," +
            "value varchar(512) NOT NULL," +
            "server_id varchar(64) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_AI("CREATE TABLE IF NOT EXISTS hcore_log_ai (" +
            "id bigint(20) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "datetime datetime NOT NULL DEFAULT current_timestamp()," +
            "player_id int(10) UNSIGNED NOT NULL," +
            "value varchar(512) NOT NULL," +
            "ip varchar(15) NOT NULL" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    LOG_MESSAGE_CONSTRAINT               ("ALTER TABLE hcore_log_message ADD CONSTRAINT hcore_log_message_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
    LOG_COMMAND_CONSTRAINT               ("ALTER TABLE hcore_log_command ADD CONSTRAINT hcore_log_command_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
    LOG_SIGN_CONSTRAINT                  ("ALTER TABLE hcore_log_sign ADD CONSTRAINT hcore_log_sign_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
    LOG_CANCELLED_PACKET_CONSTRAINT      ("ALTER TABLE hcore_log_cancelled_packet ADD CONSTRAINT hcore_log_cancelled_packet_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
    LOG_AI_CONSTRAINT                    ("ALTER TABLE hcore_log_ai ADD CONSTRAINT hcore_log_ai_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),

    LOG_MESSAGE_INDEX_DATETIME           ("ALTER TABLE hcore_log_message ADD INDEX IF NOT EXISTS idx_hcore_log_message_datetime(datetime)"),
    LOG_MESSAGE_INDEX_VALUE              ("ALTER TABLE hcore_log_message ADD INDEX IF NOT EXISTS idx_hcore_log_message_value(value)"),
    LOG_MESSAGE_INDEX_WORLD              ("ALTER TABLE hcore_log_message ADD INDEX IF NOT EXISTS idx_hcore_log_message_world(world)"),
    LOG_MESSAGE_INDEX_PROXY_SERVER       ("ALTER TABLE hcore_log_message ADD INDEX IF NOT EXISTS idx_hcore_log_message_proxy_server(proxy_server)"),
    LOG_MESSAGE_INDEX_SERVER_ID          ("ALTER TABLE hcore_log_message ADD INDEX IF NOT EXISTS idx_hcore_log_message_server_id(server_id)"),
    LOG_MESSAGE_INDEX_VALUE_FULLTEXT     ("ALTER TABLE hcore_log_message ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_message_value_fulltext(value)"),

    LOG_COMMAND_INDEX_DATETIME           ("ALTER TABLE hcore_log_command ADD INDEX IF NOT EXISTS idx_hcore_log_command_datetime(datetime)"),
    LOG_COMMAND_INDEX_VALUE              ("ALTER TABLE hcore_log_command ADD INDEX IF NOT EXISTS idx_hcore_log_command_value(value)"),
    LOG_COMMAND_INDEX_WORLD              ("ALTER TABLE hcore_log_command ADD INDEX IF NOT EXISTS idx_hcore_log_command_world(world)"),
    LOG_COMMAND_INDEX_PROXY_SERVER       ("ALTER TABLE hcore_log_command ADD INDEX IF NOT EXISTS idx_hcore_log_command_proxy_server(proxy_server)"),
    LOG_COMMAND_INDEX_SERVER_ID          ("ALTER TABLE hcore_log_command ADD INDEX IF NOT EXISTS idx_hcore_log_command_server_id(server_id)"),
    LOG_COMMAND_INDEX_VALUE_FULLTEXT     ("ALTER TABLE hcore_log_command ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_command_value_fulltext(value)"),

    LOG_SIGN_INDEX_DATETIME              ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_datetime(datetime)"),

    LOG_SIGN_INDEX_LINE_1                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_1(line_1)"),
    LOG_SIGN_INDEX_LINE_2                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_2(line_2)"),
    LOG_SIGN_INDEX_LINE_3                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_3(line_3)"),
    LOG_SIGN_INDEX_LINE_4                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_4(line_4)"),
    LOG_SIGN_INDEX_LINE_5                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_5(line_5)"),
    LOG_SIGN_INDEX_LINE_6                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_6(line_6)"),
    LOG_SIGN_INDEX_LINE_7                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_7(line_7)"),
    LOG_SIGN_INDEX_LINE_8                ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_line_8(line_8)"),
    LOG_SIGN_INDEX_LINE_1_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_1_fulltext(line_1)"),
    LOG_SIGN_INDEX_LINE_2_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_2_fulltext(line_2)"),
    LOG_SIGN_INDEX_LINE_3_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_3_fulltext(line_3)"),
    LOG_SIGN_INDEX_LINE_4_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_4_fulltext(line_4)"),
    LOG_SIGN_INDEX_LINE_5_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_5_fulltext(line_5)"),
    LOG_SIGN_INDEX_LINE_6_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_6_fulltext(line_6)"),
    LOG_SIGN_INDEX_LINE_7_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_7_fulltext(line_7)"),
    LOG_SIGN_INDEX_LINE_8_FULLTEXT       ("ALTER TABLE hcore_log_sign ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_sign_line_8_fulltext(line_8)"),

    LOG_SIGN_INDEX_WORLD                 ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_world(world)"),
    LOG_SIGN_INDEX_SERVER_ID             ("ALTER TABLE hcore_log_sign ADD INDEX IF NOT EXISTS idx_hcore_log_sign_server_id(server_id)"),

    LOG_CANCELLED_PACKET_INDEX_DATETIME  ("ALTER TABLE hcore_log_cancelled_packet ADD INDEX IF NOT EXISTS idx_hcore_log_cancelled_packet_datetime(datetime)"),
    LOG_CANCELLED_PACKET_INDEX_VALUE     ("ALTER TABLE hcore_log_cancelled_packet ADD INDEX IF NOT EXISTS idx_hcore_log_cancelled_packet_value(value)"),
    LOG_CANCELLED_PACKET_INDEX_SERVER_ID ("ALTER TABLE hcore_log_cancelled_packet ADD INDEX IF NOT EXISTS idx_hcore_log_cancelled_packet_server_id(server_id)"),

    LOG_AI_INDEX_DATETIME                ("ALTER TABLE hcore_log_ai ADD INDEX IF NOT EXISTS idx_hcore_log_ai_datetime(datetime)"),
    LOG_AI_INDEX_VALUE                   ("ALTER TABLE hcore_log_ai ADD INDEX IF NOT EXISTS idx_hcore_log_ai_value(value)"),
    LOG_AI_INDEX_IP                      ("ALTER TABLE hcore_log_ai ADD INDEX IF NOT EXISTS idx_hcore_log_ai_ip(ip)"),
    LOG_AI_INDEX_VALUE_FULLTEXT          ("ALTER TABLE hcore_log_ai ADD FULLTEXT INDEX IF NOT EXISTS idx_hcore_log_ai_value_fulltext(value)"),

    PLAYER("CREATE TABLE IF NOT EXISTS hcore_player (" +
            "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
            "name varchar(16) NOT NULL UNIQUE KEY" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),

    ITEMSKIN("CREATE TABLE IF NOT EXISTS hcore_itemskin (" +
            "player_id int(10) UNSIGNED NOT NULL," +
            "skinned_item varchar(64) NOT NULL," +
            "server_id varchar(64) NOT NULL," +
            "UNIQUE KEY (player_id, skinned_item, server_id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
    ),
    ITEMSKIN_CONSTRAINT("ALTER TABLE hcore_itemskin ADD CONSTRAINT hcore_itemskin_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

    public enum Clans implements MySQLTable {
        LIST("CREATE TABLE IF NOT EXISTS hclans_list (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "name varchar(128) NOT NULL," +
                "slots int(10) UNSIGNED NOT NULL," +
                "welcome text DEFAULT NULL," +
                "pvp tinyint(1) UNSIGNED NOT NULL DEFAULT '1'," +
                "balance bigint(20) UNSIGNED NOT NULL DEFAULT '0'," +
                "world varchar(64) DEFAULT NULL," +
                "x double DEFAULT NULL," +
                "y double DEFAULT NULL," +
                "z double DEFAULT NULL," +
                "yaw double DEFAULT NULL," +
                "pitch double DEFAULT NULL," +
                "created datetime NOT NULL DEFAULT current_timestamp()," +
                "server_id varchar(64) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),

        MEMBER("CREATE TABLE IF NOT EXISTS hclans_member (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "clan_id int(10) UNSIGNED NOT NULL," +
                "player_id int(10) UNSIGNED NOT NULL," +
                "is_moder tinyint(1) UNSIGNED NOT NULL DEFAULT '0'," +
                "is_leader tinyint(1) UNSIGNED NOT NULL DEFAULT '0'," +
                "last_online datetime NOT NULL DEFAULT current_timestamp()," +
                "first_join datetime NOT NULL DEFAULT current_timestamp()" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),

        LOG("CREATE TABLE IF NOT EXISTS hclans_log (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "clan_id int(10) UNSIGNED NOT NULL," +
                "player_id int(10) UNSIGNED NOT NULL," +
                "text text NOT NULL," +
                "datetime datetime NOT NULL DEFAULT current_timestamp()" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),

        MEMBER_CONSTRAINT_1("ALTER TABLE hclans_member ADD CONSTRAINT hclans_member_ibfk_1 FOREIGN KEY IF NOT EXISTS (clan_id) REFERENCES hclans_list (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        MEMBER_CONSTRAINT_2("ALTER TABLE hclans_member ADD CONSTRAINT hclans_member_ibfk_2 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        LOG_CONSTRAINT_1("ALTER TABLE hclans_log ADD CONSTRAINT hclans_log_ibfk_1 FOREIGN KEY IF NOT EXISTS (clan_id) REFERENCES hclans_list (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        LOG_CONSTRAINT_2("ALTER TABLE hclans_log ADD CONSTRAINT hclans_log_ibfk_2 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Clans(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Marry implements MySQLTable {
        COUPLE("CREATE TABLE IF NOT EXISTS hmarry_couple (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "male int(10) UNSIGNED NOT NULL," +
                "female int(10) UNSIGNED NOT NULL," +
                "pvp tinyint(1) UNSIGNED NOT NULL DEFAULT '1'," +
                "world varchar(64) DEFAULT NULL," +
                "x double DEFAULT NULL," +
                "y double DEFAULT NULL," +
                "z double DEFAULT NULL," +
                "yaw double DEFAULT NULL," +
                "pitch double DEFAULT NULL," +
                "created datetime NOT NULL DEFAULT current_timestamp()," +
                "server_id varchar(64) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),

        PLAYER("CREATE TABLE IF NOT EXISTS hmarry_player (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "player_id int(10) UNSIGNED NOT NULL," +
                "gender tinyint(1) UNSIGNED NOT NULL DEFAULT '0'," +
                "datetime datetime NOT NULL DEFAULT current_timestamp()," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),

        KISS("CREATE TABLE IF NOT EXISTS hmarry_kiss (" +
                "notify_player int(10) UNSIGNED NOT NULL," +
                "about_player int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (notify_player, about_player, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
        ),

        COUPLE_MALE_CONSTRAINT("ALTER TABLE hmarry_couple ADD CONSTRAINT hmarry_couple_ibfk_1 FOREIGN KEY IF NOT EXISTS (male) REFERENCES hmarry_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        COUPLE_FEMALE_CONSTRAINT("ALTER TABLE hmarry_couple ADD CONSTRAINT hmarry_couple_ibfk_2 FOREIGN KEY IF NOT EXISTS (female) REFERENCES hmarry_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        PLAYER_CONSTRAINT("ALTER TABLE hmarry_player ADD CONSTRAINT hmarry_player_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        KISS_CONSTRAINT_1("ALTER TABLE hmarry_kiss ADD CONSTRAINT hmarry_kiss_ibfk_1 FOREIGN KEY IF NOT EXISTS (notify_player) REFERENCES hmarry_player (id) ON DELETE CASCADE ON UPDATE CASCADE"),
        KISS_CONSTRAINT_2("ALTER TABLE hmarry_kiss ADD CONSTRAINT hmarry_kiss_ibfk_2 FOREIGN KEY IF NOT EXISTS (about_player) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Marry(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Contracts implements MySQLTable {
        DATA("CREATE TABLE IF NOT EXISTS hcontracts_data (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "value int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        DATA_CONSTRAINT("ALTER TABLE hcontracts_data ADD CONSTRAINT hcontracts_data_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Contracts(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Sex implements MySQLTable {
        DATA("CREATE TABLE IF NOT EXISTS hsex_data (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "value int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        DATA_CONSTRAINT("ALTER TABLE hsex_data ADD CONSTRAINT hsex_data_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Sex(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Cinema implements MySQLTable {
        VIDEO("CREATE TABLE IF NOT EXISTS hcinema_video (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "service_type varchar(16) NOT NULL," +
                "service_id varchar(255) NOT NULL," +
                "title text NOT NULL," +
                "duration_seconds bigint(20) UNSIGNED NOT NULL," +
                "UNIQUE (service_type, service_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        THEATER("CREATE TABLE IF NOT EXISTS hcinema_theater (" +
                "name varchar(64) NOT NULL," +
                "region varchar(64) NOT NULL," +
                "world varchar(64) NOT NULL," +
                "x double NOT NULL," +
                "y double NOT NULL," +
                "z double NOT NULL," +
                "facing varchar(5) NOT NULL," +
                "width tinyint(3) UNSIGNED NOT NULL," +
                "height tinyint(3) UNSIGNED NOT NULL," +
                "muted tinyint(1) UNSIGNED NOT NULL," +
                "url text DEFAULT NULL," +
                "server_id varchar(64) NOT NULL," +
                "PRIMARY KEY (name, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci");

        private final String value;

        Cinema(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Images implements MySQLTable {
        DATA("CREATE TABLE IF NOT EXISTS himages_data (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "server_id varchar(64) NOT NULL," +
                "data longblob NOT NULL," +
                "permanently tinyint(1) UNSIGNED NOT NULL DEFAULT 0" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        PLAYER("CREATE TABLE IF NOT EXISTS himages_player (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "value int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        PLAYER_CONSTRAINT("ALTER TABLE himages_player ADD CONSTRAINT himages_player_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Images(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Crates implements MySQLTable {
        LOCATION("CREATE TABLE IF NOT EXISTS hcrates_location (" +
                "id int(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                "crate varchar(64) NOT NULL," +
                "world varchar(64) NOT NULL," +
                "x double NOT NULL," +
                "y double NOT NULL," +
                "z double NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (crate, world, x, y, z, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        KEY("CREATE TABLE IF NOT EXISTS hcrates_key (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "crate varchar(64) NOT NULL," +
                "count int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, crate, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        KEY_CONSTRAINT("ALTER TABLE hcrates_key ADD CONSTRAINT hcrates_key_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Crates(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Protect implements MySQLTable {
        REGION("CREATE TABLE IF NOT EXISTS hprotect_region (" +
                "name varchar(64) NOT NULL," +
                "world varchar(64) NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (name, world, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        BYPASS("CREATE TABLE IF NOT EXISTS hprotect_bypass (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (player_id, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        BYPASS_CONSTRAINT("ALTER TABLE hprotect_bypass ADD CONSTRAINT hprotect_bypass_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Protect(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    public enum Kalian implements MySQLTable {
        DATA("CREATE TABLE IF NOT EXISTS hkalian_data (" +
                "player_id int(10) UNSIGNED NOT NULL," +
                "x int(11) NOT NULL," +
                "y int(11) NOT NULL," +
                "z int(11) NOT NULL," +
                "world varchar(64) NOT NULL," +
                "server_id varchar(64) NOT NULL," +
                "UNIQUE KEY (x, y, z, world, server_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"),
        DATA_CONSTRAINT("ALTER TABLE hkalian_data ADD CONSTRAINT hkalian_data_ibfk_1 FOREIGN KEY IF NOT EXISTS (player_id) REFERENCES hcore_player (id) ON DELETE CASCADE ON UPDATE CASCADE");

        private final String value;

        Kalian(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }
    }

    private final String value;

    Table(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
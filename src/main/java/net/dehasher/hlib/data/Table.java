package net.dehasher.hlib.data;

import net.dehasher.hlib.database.MySQLTable;

// Тут нельзя использовать lombok, так как надо делать ОБЯЗАТЕЛЬНО @Override... :(
public enum Table implements MySQLTable {
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
    );

    private final String value;

    Table(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
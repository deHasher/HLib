package net.dehasher.hlib.data;

import net.dehasher.hlib.database.MySQLTable;

// Тут нельзя использовать lombok, так как надо делать ОБЯЗАТЕЛЬНО @Override... :(
public enum Table implements MySQLTable {
	COOLDOWN("CREATE TABLE IF NOT EXISTS hlib_cooldown (" +
			"cooldown_id varchar(512) NOT NULL," +
			"server_id varchar(64) NOT NULL," +
			"until datetime NOT NULL," +
			"UNIQUE KEY (cooldown_id, server_id)" +
			") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci"
	),

	PLAYER("CREATE TABLE IF NOT EXISTS hlib_player (" +
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
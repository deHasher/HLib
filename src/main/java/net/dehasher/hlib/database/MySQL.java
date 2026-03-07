package net.dehasher.hlib.database;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Informer;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class MySQL {
    @Setter
    @Getter
    private boolean enabled = true;
    private HikariDataSource pool;

    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public MySQL(String address, String database, String username, String password, String url, String driver, Integer keepaliveTime, Integer connectionTimeout, Integer maxLifetime, Integer poolSize) {
        if (url == null || url.isEmpty()) url = "jdbc:{driver}://{address}/{database}?useUnicode=true&characterEncoding=UTF-8";
        if (keepaliveTime == null) keepaliveTime = 300;
        if (connectionTimeout == null) connectionTimeout = 5;
        if (maxLifetime == null) maxLifetime = 1800;
        if (poolSize == null) poolSize = 10;
        if (driver == null || driver.isEmpty()) driver = "mysql";

        String jdbc = url
                .replace("{address}", address)
                .replace("{database}", database)
                .replace("{driver}", driver);

        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl(jdbc);
            config.setUsername(username);
            config.setPassword(password);

            config.setKeepaliveTime(keepaliveTime * 1000L);
            config.setConnectionTimeout(connectionTimeout * 1000L);
            config.setMaxLifetime(maxLifetime * 1000L);

            config.setMaximumPoolSize(poolSize);
            config.setMinimumIdle(Math.max(1, poolSize / 2));

            switch (driver) {
                case "mariadb":
                    config.setDriverClassName("org.mariadb.jdbc.Driver");
                    break;
                default:
                    config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                    break;
            }

            pool = new HikariDataSource(config);

            Informer.send("MySQL successfully connected!");
        } catch (Throwable t) {
            Informer.send("Failed to connect to the MySQL! (" + jdbc + ")");
            t.printStackTrace();
            setEnabled(false);
            if (pool != null && !pool.isClosed()) pool.close();
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    public void shutdown() {
        setEnabled(false);
        if (pool == null || pool.isClosed()) return;
        pool.close();
    }

    @SuppressWarnings("DataFlowIssue")
    private boolean execute(String query, ThrowingConsumer<ResultSet> consumer, boolean quiet, Object... args) {
        if (!isEnabled()) return false;

        try (Connection conn = getConnection() ; PreparedStatement ps = conn.prepareStatement(query)) {
            prepare(ps, args);
            if (consumer != null) {
                try (ResultSet rs = ps.executeQuery()) {
                    consumer.accept(rs);
                    return true;
                }
            } else {
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            if (quiet) return true;
            handleSQLException(ex, query, args);
            return false;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private Optional<Integer> executeInsert(String query, boolean quiet, Object... args) {
        if (!isEnabled()) return Optional.empty();

        try (Connection conn = getConnection() ; PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            prepare(ps, args);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? Optional.of(keys.getInt(1)) : Optional.empty();
            }
        } catch (SQLException ex) {
            if (quiet) return Optional.empty();
            handleSQLException(ex, query, args);
        }
        return Optional.empty();
    }

    @SuppressWarnings("DataFlowIssue")
    private int[] executeBatch(String query, boolean quiet, List<Object[]> batchArgs) {
        if (!isEnabled() || batchArgs.isEmpty()) return new int[0];

        try (Connection conn = getConnection() ; PreparedStatement ps = conn.prepareStatement(query)) {
            for (Object[] args : batchArgs) {
                ps.clearParameters();
                prepare(ps, args);
                ps.addBatch();
            }

            return ps.executeBatch();
        } catch (SQLException ex) {
            if (quiet) return new int[0];
            handleSQLException(ex, query, null);
            return new int[0];
        }
    }

    private Connection getConnection() throws SQLException {
        if (!isEnabled()) return null;
        return pool.getConnection();
    }

    private void prepare(PreparedStatement ps, Object... args) throws SQLException {
        if (args == null) return;
        int paramCount = ps.getParameterMetaData().getParameterCount();
        if (paramCount != args.length) throw new SQLException("Parameter count mismatch: expected " + paramCount + ", got " + args.length);
        for (int i = 0; i < args.length; i++) setObject(ps, i + 1, args[i]);
    }

    private void handleSQLException(SQLException ex, String sql, Object[] args) {
        Informer.send("SQL error executing query: " + sql);
        if (args != null && args.length > 0) Informer.send("SQL args: " + IntStream.range(0, args.length)
                .mapToObj(i -> i + " => " + args[i])
                .reduce((s1, s2) -> s1 + ", " + s2)
                .orElse(""));
        ex.printStackTrace();
    }

    private void setObject(PreparedStatement ps, int index, Object obj) throws SQLException {
        if (obj == null) {
            ps.setNull(index, Types.NULL);
            return;
        }

        if (obj instanceof Byte) {
            ps.setInt(index, ((Byte) obj).intValue());
        } else if (obj instanceof String) {
            ps.setString(index, (String) obj);
        } else if (obj instanceof Enum) {
            ps.setString(index, ((Enum<?>) obj).name());
        } else if (obj instanceof BigDecimal) {
            ps.setBigDecimal(index, (BigDecimal) obj);
        } else if (obj instanceof Short) {
            ps.setShort(index, (Short) obj);
        } else if (obj instanceof Integer) {
            ps.setInt(index, (Integer) obj);
        } else if (obj instanceof Long) {
            ps.setLong(index, (Long) obj);
        } else if (obj instanceof Float) {
            ps.setFloat(index, (Float) obj);
        } else if (obj instanceof Double) {
            ps.setDouble(index, (Double) obj);
        } else if (obj instanceof byte[]) {
            ps.setBytes(index, (byte[]) obj);
        } else if (obj instanceof java.sql.Date) {
            ps.setDate(index, (java.sql.Date) obj);
        } else if (obj instanceof Time) {
            ps.setTime(index, (Time) obj);
        } else if (obj instanceof Timestamp) {
            ps.setTimestamp(index, (Timestamp) obj);
        } else if (obj instanceof Boolean) {
            ps.setBoolean(index, (Boolean) obj);
        } else if (obj instanceof InputStream) {
            ps.setBinaryStream(index, (InputStream) obj, -1);
        } else if (obj instanceof Blob) {
            ps.setBlob(index, (Blob) obj);
        } else if (obj instanceof Clob) {
            ps.setClob(index, (Clob) obj);
        } else if (obj instanceof java.util.Date) {
            ps.setTimestamp(index, new Timestamp(((java.util.Date) obj).getTime()));
        } else if (obj instanceof BigInteger) {
            ps.setObject(index, obj, Types.BIGINT);
        } else {
            ps.setObject(index, obj);
        }
    }

    public <T extends MySQLTable> Query query(T table) {
        return query(table.getValue(), true);
    }

    public Query query(String query) {
        return query(query, false);
    }

    public Query query(String query, boolean quiet) {
        return new Query(this, query, quiet);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Query {
        private final MySQL sql;
        private final String query;
        private Object[] args = null;
        private final List<Object[]> batchArgs = Lists.newArrayList();
        private ThrowingConsumer<ResultSet> consumer = null;
        private boolean quiet;

        public Query(MySQL mySQL, String query, boolean quiet) {
            this.sql = mySQL;
            this.query = query;
            this.quiet = quiet;
        }

        public Query setArgs(Object... args) {
            this.args = args;
            return this;
        }

        public Query setQuiet(boolean quiet) {
            this.quiet = quiet;
            return this;
        }

        public Query addBatch(Object... args) {
            this.batchArgs.add(args);
            return this;
        }

        public Query setResult(ThrowingConsumer<ResultSet> consumer) {
            this.consumer = consumer;
            return this;
        }

        public boolean execute() {
            return sql.execute(query, consumer, quiet, args);
        }

        public Optional<Integer> executeInsert() {
            return sql.executeInsert(query, quiet, args);
        }

        public int[] executeBatch() {
            return sql.executeBatch(query, quiet, batchArgs);
        }
    }
}
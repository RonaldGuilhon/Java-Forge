package com.javaforge.database.connector;

import java.sql.*;
import java.util.*;

public class DatabaseConnector implements AutoCloseable {

    private Connection connection;
    private DatabaseType dbType;

    public enum DatabaseType {
        POSTGRESQL, MYSQL, ORACLE, SQL_SERVER, SQLITE
    }

    public static class DatabaseConfig {
        private DatabaseType type;
        private String host;
        private int port;
        private String database;
        private String username;
        private String password;

        public DatabaseConfig() {}

        public DatabaseConfig(DatabaseType type, String host, int port,
                              String database, String username, String password) {
            this.type = type;
            this.host = host;
            this.port = port;
            this.database = database;
            this.username = username;
            this.password = password;
        }

        public DatabaseType getType() { return type; }
        public void setType(DatabaseType type) { this.type = type; }
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String jdbcUrl() {
            switch (type) {
                case POSTGRESQL: return "jdbc:postgresql://" + host + ":" + port + "/" + database;
                case MYSQL: return "jdbc:mysql://" + host + ":" + port + "/" + database;
                case ORACLE: return "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
                case SQL_SERVER: return "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + database;
                case SQLITE: return "jdbc:sqlite:" + database;
                default: return "";
            }
        }

        public String driverClass() {
            switch (type) {
                case POSTGRESQL: return "org.postgresql.Driver";
                case MYSQL: return "com.mysql.cj.jdbc.Driver";
                case ORACLE: return "oracle.jdbc.OracleDriver";
                case SQL_SERVER: return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                case SQLITE: return "org.sqlite.JDBC";
                default: return "";
            }
        }
    }

    public void connect(DatabaseConfig config) throws SQLException {
        this.dbType = config.getType();
        try {
            Class.forName(config.driverClass());
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found: " + config.driverClass(), e);
        }
        if (config.getType() == DatabaseType.SQLITE) {
            connection = DriverManager.getConnection(config.jdbcUrl());
        } else {
            connection = DriverManager.getConnection(config.jdbcUrl(), config.getUsername(), config.getPassword());
        }
    }

    public List<String> listSchemas() throws SQLException {
        List<String> schemas = new ArrayList<String>();
        try (ResultSet rs = connection.getMetaData().getSchemas()) {
            while (rs.next()) schemas.add(rs.getString("TABLE_SCHEM"));
        }
        return schemas;
    }

    public List<String> listTables() throws SQLException {
        List<String> tables = new ArrayList<String>();
        String catalog = connection.getCatalog();
        String schemaPattern = dbType == DatabaseType.POSTGRESQL ? "public" : null;
        try (ResultSet rs = connection.getMetaData().getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
            while (rs.next()) tables.add(rs.getString("TABLE_NAME"));
        }
        return tables;
    }

    public List<ColumnInfo> listColumns(String table) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<ColumnInfo>();
        try (ResultSet rs = connection.getMetaData().getColumns(null, null, table, "%")) {
            while (rs.next()) {
                columns.add(new ColumnInfo(
                    rs.getString("COLUMN_NAME"),
                    rs.getString("TYPE_NAME"),
                    rs.getInt("COLUMN_SIZE"),
                    rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                    rs.getString("IS_AUTOINCREMENT") != null && rs.getString("IS_AUTOINCREMENT").equals("YES"),
                    rs.getString("REMARKS")
                ));
            }
        }
        return columns;
    }

    public List<Map<String, Object>> query(String sql) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int cols = meta.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<String, Object>();
                for (int i = 1; i <= cols; i++) {
                    row.put(meta.getColumnLabel(i), rs.getObject(i));
                }
                results.add(row);
            }
        }
        return results;
    }

    public int execute(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }

    public static class ColumnInfo {
        private String name;
        private String type;
        private int size;
        private boolean nullable;
        private boolean autoIncrement;
        private String comment;

        public ColumnInfo() {}

        public ColumnInfo(String name, String type, int size, boolean nullable, boolean autoIncrement, String comment) {
            this.name = name;
            this.type = type;
            this.size = size;
            this.nullable = nullable;
            this.autoIncrement = autoIncrement;
            this.comment = comment;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public boolean isNullable() { return nullable; }
        public void setNullable(boolean nullable) { this.nullable = nullable; }
        public boolean isAutoIncrement() { return autoIncrement; }
        public void setAutoIncrement(boolean autoIncrement) { this.autoIncrement = autoIncrement; }
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
}

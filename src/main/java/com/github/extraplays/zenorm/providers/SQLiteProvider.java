package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.connection.ConnectionManager;
import com.github.extraplays.zenorm.dialetcs.Dialect;
import com.github.extraplays.zenorm.dialetcs.SQLiteDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteProvider implements DatabaseProvider{

    private final String databasePath;
    private ConnectionManager connectionManager;

    public SQLiteProvider(String databasePath) {
        this.databasePath = databasePath;
        connect();
    }

    @Override
    public Dialect getDialect() {
        return new SQLiteDialect();
    }

    @Override
    public void connect() {
        try {
            String jdbcUrl = "jdbc:sqlite:" + databasePath;
            this.connectionManager = new ConnectionManager(jdbcUrl, null, null);
            System.out.println("[SQLiteProvider] Conectado ao banco SQLite: " + databasePath);

            try (var stmt = getConnection().createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar no banco SQLite!", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionManager.getConnection();
    }

    @Override
    public void shutdown() {
        connectionManager.shutdown();
        System.out.println("[SQLiteProvider] Conexão SQLite fechada.");
    }
}

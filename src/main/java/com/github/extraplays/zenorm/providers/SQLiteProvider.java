package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.dialetcs.Dialect;
import com.github.extraplays.zenorm.dialetcs.SQLiteDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteProvider implements DatabaseProvider{

    private final String databasePath;
    private Connection connection;

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
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            System.out.println("[SQLiteProvider] Conectado ao banco SQLite: " + databasePath);

            try (var stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar no banco SQLite!", e);
        }
    }


    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[SQLiteProvider] Conexão SQLite fechada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}

package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.dialetcs.Dialect;
import com.github.extraplays.zenorm.dialetcs.MySQLDialect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLProvider implements DatabaseProvider{

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private Connection connection;

    public MySQLProvider(String host, int port, String database, String username, String password) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;

        connect();
    }

    @Override
    public void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // novo driver MySQL
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
            this.connection = DriverManager.getConnection(url, username, password);
            System.out.println("[MySQLProvider] Conectado ao banco MySQL: " + database);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar no banco MySQL!", e);
        }
    }

    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[MySQLProvider] Conexão MySQL fechada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Dialect getDialect() {
        return new MySQLDialect();
    }
}

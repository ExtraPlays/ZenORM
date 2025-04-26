package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.connection.ConnectionManager;
import com.github.extraplays.zenorm.dialetcs.Dialect;
import com.github.extraplays.zenorm.dialetcs.MySQLDialect;

import java.sql.Connection;
import java.sql.SQLException;

public class MySQLProvider implements DatabaseProvider{

    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final int port;
    private ConnectionManager connectionManager;

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
            this.connectionManager = new ConnectionManager(url, username, password);
            System.out.println("[MySQLProvider] Conectado ao banco MySQL: " + database);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar no banco MySQL!", e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionManager.getConnection();
    }


    @Override
    public Dialect getDialect() {
        return new MySQLDialect();
    }

    @Override
    public void shutdown() {
        connectionManager.shutdown();
        System.out.println("[MySQLProvider] Conexão MySQL encerrada.");
    }
}

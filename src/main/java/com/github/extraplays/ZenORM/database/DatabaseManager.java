package com.github.extraplays.ZenORM.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

@Getter
public class DatabaseManager {

    private HikariDataSource dataSource;

    public DatabaseManager(String host, String database, String username, String password, int port) {

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        //config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        dataSource = new HikariDataSource(config);
    }

    public Connection getConnection () {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get connection", e);
        }
    }

    public void close() {
        if (dataSource !=null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}

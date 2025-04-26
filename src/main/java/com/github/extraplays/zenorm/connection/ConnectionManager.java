package com.github.extraplays.zenorm.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private final HikariDataSource dataSource;

    @Getter
    private final String jdbcUrl;

    public ConnectionManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);

        if (username != null && !username.isEmpty()) {
            config.setUsername(username);
        }

        if (password != null && !password.isEmpty()) {
            config.setPassword(password);
        }

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(60000);
        config.setConnectionTimeout(30000);
        config.setLeakDetectionThreshold(60000); // Detectar conexões travadas
        config.setPoolName("ZenORM-Pool");

        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}

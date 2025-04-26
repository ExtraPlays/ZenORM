package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.dialetcs.Dialect;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseProvider {

    //void disconnect();
    Connection getConnection() throws SQLException;
    void connect();
    Dialect getDialect();
    void shutdown();
}

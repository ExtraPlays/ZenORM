package com.github.extraplays.zenorm.providers;

import com.github.extraplays.zenorm.dialetcs.Dialect;

import java.sql.Connection;

public interface DatabaseProvider {

    void disconnect();
    Connection getConnection();
    void connect();
    Dialect getDialect();
}

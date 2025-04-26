package com.github.extraplys.zenorm.providers;

import com.github.extraplys.zenorm.dialetcs.Dialect;

import java.sql.Connection;

public interface DatabaseProvider {

    void disconnect();
    Connection getConnection();
    void connect();
    Dialect getDialect();
}

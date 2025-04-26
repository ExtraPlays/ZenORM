package com.github.extraplys.zenorm.dialetcs;

public class MySQLDialect implements Dialect{

    @Override
    public String getAutoIncrement() {
        return "AUTO_INCREMENT";
    }

    @Override
    public String getPrimaryKey() {
        return "PRIMARY KEY";
    }

    @Override
    public String getType(String originalType) {
        return originalType; // MySQL supports most types directly
    }
}

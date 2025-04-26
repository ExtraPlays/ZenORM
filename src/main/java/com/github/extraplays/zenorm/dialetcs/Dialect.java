package com.github.extraplays.zenorm.dialetcs;

public interface Dialect {
    String getAutoIncrement();
    String getPrimaryKey();
    String getType(String originalType);
}

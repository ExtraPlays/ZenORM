package com.github.extraplays.zenorm.dialetcs;

public class SQLiteDialect implements Dialect{

    @Override
    public String getAutoIncrement() {
        return "AUTOINCREMENT";
    }

    @Override
    public String getPrimaryKey() {
        return "PRIMARY KEY";
    }

    @Override
    public String getType(String originalType) {
        return switch (originalType) {
            case "int", "integer" -> "INTEGER";
            case "text", "varchar" -> "TEXT";
            case "real", "double", "float" -> "REAL";
            case "blob" -> "BLOB";
            case "boolean" -> "BOOLEAN";
            default -> originalType;
        };
    }
}

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
        switch (originalType) {
            case "int":
            case "integer":
                return "INTEGER";
            case "text":
            case "varchar":
                return "TEXT";
            case "real":
            case "double":
            case "float":
                return "REAL";
            case "blob":
                return "BLOB";
            case "boolean":
                return "BOOLEAN";
            default:
                return originalType;
        }
    }
}

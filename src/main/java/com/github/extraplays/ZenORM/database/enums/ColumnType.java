package com.github.extraplays.ZenORM.database.enums;

import lombok.Getter;

@Getter
public enum ColumnType {

    INT("INT"),
    FLOAT("FLOAT"),
    BIGINT("BIGINT"),
    VARCHAR("VARCHAR"),
    TEXT("TEXT"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    TIME("TIME"),
    TIMESTAMP("TIMESTAMP"),
    BLOB("BLOB"),
    JSON("JSON"),
    ;

    private final String sqlType;

    ColumnType(String sqlType) {
        this.sqlType = sqlType;
    }

}

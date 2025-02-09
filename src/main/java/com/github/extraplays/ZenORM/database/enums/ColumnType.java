package com.github.extraplays.ZenORM.database.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ColumnType {

    INT("INT"),
    FLOAT("FLOAT"),
    DECIMAL("DECIMAL"),
    BIGINT("BIGINT"),
    VARCHAR("VARCHAR"),
    TEXT("TEXT"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    TIME("TIME"),
    DATETIME("DATETIME"),
    TIMESTAMP("TIMESTAMP");

    private final String type;
}
package com.github.extraplays.ZenORM.database.annotations;

import com.github.extraplays.ZenORM.database.enums.ColumnType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    ColumnType type();
}

package com.github.extraplays.ZenORM.database.annotations;

import com.github.extraplays.ZenORM.database.enums.ColumnType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name();
    ColumnType type();
    boolean nullable() default true;
    boolean unique() default false;
    boolean primary() default false;
    boolean autoIncrement() default false;
    int length() default 255;
    int precision() default 0;
    int scale() default 0;
    String defaultValue() default "";
}
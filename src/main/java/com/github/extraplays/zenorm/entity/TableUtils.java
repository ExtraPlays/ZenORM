package com.github.extraplays.zenorm.entity;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.Table;

import java.lang.reflect.Field;

public class TableUtils {

    public static String getTableName(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("Missing @Table annotation in class " + clazz.getName());
        }
        Table table = clazz.getAnnotation(Table.class);
        return table.name();
    }

    public static String getIdColumn(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    return column.name();
                }
            }
        }
        throw new RuntimeException("No primary key column found in class " + clazz.getName());
    }

    public static Field getPrimaryKeyField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.primaryKey()) {
                    return field;
                }
            }
        }
        throw new RuntimeException("No primary key field found in class " + clazz.getName());
    }

}

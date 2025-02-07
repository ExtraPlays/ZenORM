package com.github.extraplays.ZenORM.database.impl;

import com.github.extraplays.ZenORM.database.DatabaseManager;
import com.github.extraplays.ZenORM.database.annotations.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class TableGenerator {

    public static void createTable(Class<?> clazz) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("@Table annotation not found in class " + clazz.getName());
        }

        Table table = clazz.getAnnotation(Table.class);
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table.name() + " (");

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {

            String columnName = field.getName();
            String columnType = getColumnType(field);

            if (columnType.isEmpty()) continue;

            sql.append(columnName).append(" ").append(columnType).append(", ");
        }

        sql.setLength(sql.length() - 2); // remove the last comma
        sql.append(");");

        try (Connection conn = DatabaseManager.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql.toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static String getColumnType(Field field) {

        StringBuilder columnDefinition = new StringBuilder();

        if (field.isAnnotationPresent(Id.class)) {
            return "INT AUTO_INCREMENT PRIMARY KEY";
        }
        if (field.isAnnotationPresent(Varchar.class)) {
            Varchar varchar = field.getAnnotation(Varchar.class);
            return "VARCHAR(" + varchar.size() + ")";
        }
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            return column.type().getSqlType();
        }

        if (field.isAnnotationPresent(Default.class)) {
            Default defaultAnnotation = field.getAnnotation(Default.class);
            columnDefinition.append(" DEFAULT ").append("'").append(defaultAnnotation.value()).append("'");
        }
        return columnDefinition.toString();
    }

}

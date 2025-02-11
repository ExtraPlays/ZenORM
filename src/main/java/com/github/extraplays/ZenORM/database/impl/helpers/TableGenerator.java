package com.github.extraplays.ZenORM.database.impl.helpers;

import com.github.extraplays.ZenORM.database.annotations.*;
import com.github.extraplays.ZenORM.database.enums.ColumnType;

import java.lang.reflect.Field;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class TableGenerator {

    public static void createTable(Class<?> clazz, DataSource dataSource) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class must have the @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table.name() + " (");

        List<String> primaryKeys = new ArrayList<>();

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                StringBuilder columnDef = new StringBuilder(column.name() + " " + column.type().name());
                switch (column.type()){
                    case VARCHAR:
                        columnDef.append("(").append(column.length()).append(")");
                        break;
                    case DECIMAL:
                        columnDef.append("(").append(column.precision()).append(",").append(column.scale()).append(")");
                        break;
                }

                if (column.autoIncrement()) {
                    columnDef.append(" AUTO_INCREMENT");
                }

                if (!column.nullable()) {
                    columnDef.append(" NOT NULL");
                }

                if (!column.defaultValue().isEmpty()) {

                    if (column.type() == ColumnType.TIMESTAMP && "CURRENT_TIMESTAMP".equals(column.defaultValue())){
                        columnDef.append(" DEFAULT CURRENT_TIMESTAMP");
                    }else {
                        columnDef.append(" DEFAULT '").append(column.defaultValue()).append("'");
                    }

                }

                if (column.primary()) {
                    primaryKeys.add(column.name());
                }

                if (column.unique()) {
                    columnDef.append(" UNIQUE");
                }

                sql.append(columnDef).append(", ");
            }
        }

        if (!primaryKeys.isEmpty()) {
            sql.append("PRIMARY KEY (").append(String.join(", ", primaryKeys)).append("), ");
        }

        sql.delete(sql.length() - 2, sql.length());
        sql.append(");");

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql.toString());
        } catch (Exception e) {
            throw new RuntimeException("Error while creating table", e);
        }

    }
}
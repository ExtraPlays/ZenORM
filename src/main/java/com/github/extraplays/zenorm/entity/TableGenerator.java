package com.github.extraplays.zenorm.entity;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.Embedded;
import com.github.extraplays.zenorm.annotations.Table;
import com.github.extraplays.zenorm.dialetcs.Dialect;
import com.github.extraplays.zenorm.providers.DatabaseProvider;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableGenerator {

    public static void generateTable(Class<?> clazz, DatabaseProvider provider, Dialect dialect) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Missing @Table annotation in class: " + clazz.getName());
        }

        String tableName = clazz.getAnnotation(Table.class).name();
        List<String> columns = collectColumns(clazz, dialect);

        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + String.join(", ", columns) + ")";

        try (var stmt = provider.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[TableGenerator] ✅ Tabela criada: " + tableName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate table: " + tableName, e);
        }
    }

    public static void migrateTable(Class<?> clazz, DatabaseProvider provider, Dialect dialect) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("Missing @Table annotation in class " + clazz.getName());
        }

        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name();

        try (var connection = provider.getConnection()) {

            // Primeiro tenta criar a tabela (caso não exista)
            generateTable(clazz, provider, dialect);

            var metaData = connection.getMetaData();
            var columnsRs = metaData.getColumns(null, null, tableName, null);

            List<String> existingColumns = new ArrayList<>();
            while (columnsRs.next()) {
                existingColumns.add(columnsRs.getString("COLUMN_NAME").toLowerCase());
            }
            columnsRs.close();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();

                    if (!existingColumns.contains(columnName.toLowerCase())) {
                        String sql = "ALTER TABLE " + tableName +
                            " ADD COLUMN " + columnName + " " + dialect.getType(column.type());

                        if (!column.nullable()) sql += " NOT NULL";
                        if (column.unique()) sql += " UNIQUE";

                        try (var alterStmt = connection.createStatement()) {
                            alterStmt.executeUpdate(sql);
                            System.out.println("[TableGenerator] Coluna adicionada: " + columnName + " na tabela " + tableName);
                        }
                    }
                }

                if (field.isAnnotationPresent(Embedded.class)) {
                    Object embeddedObject = field.getType().getDeclaredConstructor().newInstance();
                    for (Field embeddedField : embeddedObject.getClass().getDeclaredFields()) {
                        embeddedField.setAccessible(true);

                        if (embeddedField.isAnnotationPresent(Column.class)) {
                            Column embeddedColumn = embeddedField.getAnnotation(Column.class);
                            String prefixedName = field.getName() + "_" + embeddedColumn.name();

                            if (!existingColumns.contains(prefixedName.toLowerCase())) {
                                String sql = "ALTER TABLE " + tableName +
                                    " ADD COLUMN " + prefixedName + " " + dialect.getType(embeddedColumn.type());

                                if (!embeddedColumn.nullable()) sql += " NOT NULL";
                                if (embeddedColumn.unique()) sql += " UNIQUE";

                                try (var alterStmt = connection.createStatement()) {
                                    alterStmt.executeUpdate(sql);
                                    System.out.println("[TableGenerator] Coluna adicionada: " + prefixedName + " na tabela " + tableName);
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to migrate table: " + tableName, e);
        }
    }

    private static List<String> collectColumns(Class<?> clazz, Dialect dialect) {
        List<String> columns = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Column.class)) {
                columns.add(buildColumnDefinition(field.getAnnotation(Column.class), dialect));
            }

            if (field.isAnnotationPresent(Embedded.class)) {
                for (Field embeddedField : field.getType().getDeclaredFields()) {
                    embeddedField.setAccessible(true);

                    if (embeddedField.isAnnotationPresent(Column.class)) {
                        Column embeddedColumn = embeddedField.getAnnotation(Column.class);
                        String prefixedName = field.getName() + "_" + embeddedColumn.name();
                        columns.add(buildColumnDefinition(prefixedName, embeddedColumn, dialect));
                    }
                }
            }
        }

        return columns;
    }

    private static String buildColumnDefinition(Column column, Dialect dialect) {
        return buildColumnDefinition(column.name(), column, dialect);
    }

    private static String buildColumnDefinition(String columnName, Column column, Dialect dialect) {
        StringBuilder sb = new StringBuilder();
        sb.append(columnName).append(" ").append(dialect.getType(column.type()));

        if (column.primaryKey()) {
            sb.append(" ").append(dialect.getPrimaryKey());
        }
        if (column.autoIncrement()) {
            sb.append(" ").append(dialect.getAutoIncrement());
        }
        if (!column.nullable()) {
            sb.append(" NOT NULL");
        }
        if (column.unique()) {
            sb.append(" UNIQUE");
        }
        return sb.toString();
    }
}

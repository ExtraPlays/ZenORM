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
            throw new RuntimeException("Missing @Table annotation in class " + clazz.getName());
        }

        Table table = clazz.getAnnotation(Table.class);
        List<String> columns = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);

            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);

                StringBuilder columnDef = new StringBuilder();
                columnDef.append(column.name()).append(" ").append(dialect.getType(column.type()));

                if (column.primaryKey()) {
                    columnDef.append(" ").append(dialect.getPrimaryKey());
                }

                if (column.autoIncrement()) {
                    columnDef.append(" ").append(dialect.getAutoIncrement());
                }

                if (!column.nullable()) {
                    columnDef.append(" NOT NULL");
                }

                if (column.unique()) {
                    columnDef.append(" UNIQUE");
                }

                columns.add(columnDef.toString());
            }

            if (field.isAnnotationPresent(Embedded.class)) {
                // Se o campo for @Embedded, percorre os campos internos
                for (Field embeddedField : field.getType().getDeclaredFields()) {
                    embeddedField.setAccessible(true);

                    if (embeddedField.isAnnotationPresent(Column.class)) {
                        Column column = embeddedField.getAnnotation(Column.class);

                        String prefixedColumnName = field.getName() + "_" + column.name();

                        StringBuilder columnDef = new StringBuilder();
                        columnDef.append(prefixedColumnName).append(" ").append(dialect.getType(column.type()));

                        if (!column.nullable()) {
                            columnDef.append(" NOT NULL");
                        }

                        columns.add(columnDef.toString());
                    }
                }
            }
        }

        String sql = "CREATE TABLE IF NOT EXISTS " + table.name() + " (" + String.join(", ", columns) + ")";

        try (Statement stmt = provider.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
            System.out.println("[TableGenerator] Tabela gerada: " + table.name());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate table for class " + clazz.getName(), e);
        }
    }

    public static void migrateTable(Class<?> clazz, DatabaseProvider provider, Dialect dialect) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new RuntimeException("Missing @Table annotation in class " + clazz.getName());
        }

        Table table = clazz.getAnnotation(Table.class);
        String tableName = table.name();

        try (var connection = provider.getConnection();
             var statement = connection.createStatement()) {

            var metaData = connection.getMetaData();
            var tables = metaData.getTables(null, null, tableName, null);

            boolean tableExists = tables.next();

            if (!tableExists) {
                generateTable(clazz, provider, dialect);
                System.out.println("[TableGenerator] Tabela criada: " + tableName);
                return;
            }

            var columnsRs = metaData.getColumns(null, null, tableName, null);

            List<String> existingColumns = new ArrayList<>();
            while (columnsRs.next()) {
                existingColumns.add(columnsRs.getString("COLUMN_NAME").toLowerCase());
            }

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String columnName = column.name();

                    if (!existingColumns.contains(columnName.toLowerCase())) {
                        // Adicionar coluna nova
                        String sql = "ALTER TABLE " + tableName +
                            " ADD COLUMN " + columnName + " " + dialect.getType(column.type());

                        if (!column.nullable()) {
                            sql += " NOT NULL";
                        }

                        if (column.unique()) {
                            sql += " UNIQUE";
                        }

                        try (var alterStatement = connection.createStatement()) {
                            alterStatement.executeUpdate(sql);
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

                                if (!embeddedColumn.nullable()) {
                                    sql += " NOT NULL";
                                }

                                if (embeddedColumn.unique()) {
                                    sql += " UNIQUE";
                                }

                                try (var alterStatement = connection.createStatement()) {
                                    alterStatement.executeUpdate(sql);
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
}

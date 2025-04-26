package com.github.extraplays.zenorm.entity;

import com.github.extraplays.zenorm.annotations.Column;
import com.github.extraplays.zenorm.annotations.Embedded;
import com.github.extraplays.zenorm.providers.DatabaseProvider;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class EntityMapper {

    public static <T> void save(T entity, DatabaseProvider provider) {
        try {
            Class<?> clazz = entity.getClass();
            String tableName = TableUtils.getTableName(clazz);

            List<String> columns = new ArrayList<>();
            List<Object> values = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);

                    if (column.autoIncrement()) continue; // Ignorar autoIncrement no insert

                    columns.add(column.name());
                    values.add(field.get(entity));
                }

                if (field.isAnnotationPresent(Embedded.class)) {
                    Object embeddedObject = field.get(entity);

                    if (embeddedObject != null) {
                        for (Field embeddedField : embeddedObject.getClass().getDeclaredFields()) {
                            embeddedField.setAccessible(true);

                            if (embeddedField.isAnnotationPresent(Column.class)) {
                                Column embeddedColumn = embeddedField.getAnnotation(Column.class);

                                String prefixedName = field.getName() + "_" + embeddedColumn.name();
                                columns.add(prefixedName);
                                values.add(embeddedField.get(embeddedObject));
                            }
                        }
                    }
                }
            }

            StringBuilder sql = new StringBuilder("INSERT INTO ").append(tableName).append(" (")
                .append(String.join(", ", columns)).append(") VALUES (")
                .append(String.join(", ", columns.stream().map(c -> "?").toList()))
                .append(")");

            try (PreparedStatement stmt = provider.getConnection().prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to save entity: " + entity.getClass().getName(), e);
        }
    }

    public static <T> void update(T entity, DatabaseProvider provider) {
        try {
            Class<?> clazz = entity.getClass();
            String tableName = TableUtils.getTableName(clazz);
            String idColumn = TableUtils.getIdColumn(clazz);

            List<String> updates = new ArrayList<>();
            List<Object> values = new ArrayList<>();
            Object idValue = null;

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    Object value = field.get(entity);

                    if (column.primaryKey()) {
                        idValue = value;
                    } else {
                        updates.add(column.name() + " = ?");
                        values.add(value);
                    }
                }

                if (field.isAnnotationPresent(Embedded.class)) {
                    Object embeddedObject = field.get(entity);

                    if (embeddedObject != null) {
                        for (Field embeddedField : embeddedObject.getClass().getDeclaredFields()) {
                            embeddedField.setAccessible(true);

                            if (embeddedField.isAnnotationPresent(Column.class)) {
                                Column embeddedColumn = embeddedField.getAnnotation(Column.class);

                                String prefixedName = field.getName() + "_" + embeddedColumn.name();
                                updates.add(prefixedName + " = ?");
                                values.add(embeddedField.get(embeddedObject));
                            }
                        }
                    }
                }
            }

            if (idValue == null) {
                throw new RuntimeException("Cannot update entity without primary key value: " + clazz.getName());
            }

            String sql = "UPDATE " + tableName + " SET " + String.join(", ", updates) + " WHERE " + idColumn + " = ?";

            try (PreparedStatement stmt = provider.getConnection().prepareStatement(sql)) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                stmt.setObject(values.size() + 1, idValue);
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to update entity: " + entity.getClass().getName(), e);
        }
    }


    public static <T> void delete(T entity, DatabaseProvider provider) {

        try {
            Class<?> clazz = entity.getClass();
            String tableName = TableUtils.getTableName(clazz);
            String idColumn = TableUtils.getIdColumn(clazz);

            Object idValue = null;

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.primaryKey()) {
                        field.setAccessible(true);
                        idValue = field.get(entity);
                        break;
                    }
                }
            }

            if (idValue == null) {
                throw new RuntimeException("Cannot delete entity without primary key value: " + clazz.getName());
            }

            String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + "=?";
            try (PreparedStatement stmt = provider.getConnection().prepareStatement(sql)) {
                stmt.setObject(1, idValue);
                stmt.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> Optional<T> findById(Class<T> entityClass, DatabaseProvider provider, Object id) {
        try {
            String tableName = TableUtils.getTableName(entityClass);
            String idColumn = TableUtils.getIdColumn(entityClass);

            ResultSet rs;
            try (Statement stmt = provider.getConnection().createStatement()) {
                rs = stmt.executeQuery("SELECT * FROM " + tableName + " WHERE " + idColumn + "='" + id + "'");
            }

            if (rs.next()) {
                return Optional.of(ResultSetMapper.mapResultSet(entityClass, rs));
            }

            return Optional.empty();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> Optional<T> findOne(Class<T> entityClass, DatabaseProvider provider, String query, Object[] params) {
        try {
            String tableName = TableUtils.getTableName(entityClass);
            String sql = "SELECT * FROM " + tableName + " WHERE " + query;

            try (PreparedStatement stmt = provider.getConnection().prepareStatement(sql)) {
                for (int i = 0; i < params.length; i++) {
                    stmt.setObject(i + 1, params[i]);
                }

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(ResultSetMapper.mapResultSet(entityClass, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public static <T> List<T> findManyByCondition(Class<T> entityClass, DatabaseProvider provider, String condition, Object... params) {
        List<T> results = new ArrayList<>();
        try {
            String tableName = TableUtils.getTableName(entityClass);

            String sql = "SELECT * FROM " + tableName + " WHERE " + condition;

            var stmt = provider.getConnection().prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            var rs = stmt.executeQuery();
            List<Map<String, Object>> rows = new ArrayList<>();

            var metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                rows.add(row);
            }

            // Agora o ResultSet já está fechado, seguro.
            for (Map<String, Object> row : rows) {
                T instance = entityClass.getDeclaredConstructor().newInstance();

                for (Field field : entityClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        field.setAccessible(true);
                        Column column = field.getAnnotation(Column.class);
                        Object value = row.get(column.name());

                        if (value != null) {
                            if (field.getType().isEnum()) {
                                field.set(instance, Enum.valueOf((Class<Enum>) field.getType(), value.toString()));
                            } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                                field.set(instance, Boolean.parseBoolean(value.toString()));
                            } else {
                                field.set(instance, value);
                            }
                        }
                    }
                }
                results.add(instance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }


    public static <T> List<T> findAll(Class<T> entityClass, DatabaseProvider provider) {
        List<T> results = new ArrayList<>();

        try {
            String tableName = TableUtils.getTableName(entityClass);
            Statement stmt = provider.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);

            while (rs.next()) {
                results.add(ResultSetMapper.mapResultSet(entityClass, rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
}

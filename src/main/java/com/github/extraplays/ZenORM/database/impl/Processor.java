package com.github.extraplays.ZenORM.database.impl;

import com.github.extraplays.ZenORM.database.annotations.Table;
import com.github.extraplays.ZenORM.database.annotations.Column;
import com.github.extraplays.ZenORM.database.interfaces.ORM;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.*;

public class Processor<T> implements ORM<T> {

    // TODO: improve sql queries, asynchrony, and add more methods

    private final Class<T> clazz;
    private final HikariDataSource dataSource;

    public Processor(Class<T> clazz, HikariDataSource dataSource) {
        this.clazz = clazz;
        this.dataSource = dataSource;
        TableGenerator.createTable(clazz, dataSource);
    }

    @Override
    public void save(T object) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Field idField = getIdField(clazz);
        idField.setAccessible(true);

        try {
            Object idValue = idField.get(object);

            System.out.println("ID: " + idValue);

            if (idValue == null || (idValue instanceof Number && ((Number) idValue).intValue() == 0) || !recordExists(idField.getAnnotation(Column.class).name(), idValue)) {
                insert(object);
            } else {
                update(object);
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access ID field", e);
        }

    }

    @Override
    public void insert(T object) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);

        StringBuilder sql = new StringBuilder("INSERT INTO " + table.name() + " (");
        List<Object> values = new ArrayList<>();
        List<Field> fields = new ArrayList<>();

        long start = System.currentTimeMillis();

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                if (column.autoIncrement()) continue;

                sql.append(column.name()).append(", ");
                field.setAccessible(true);

                try {
                    Object value = field.get(object);
                    if (value == null && !column.defaultValue().isEmpty()) {
                        value = parseDefaultValue(column.defaultValue(), field.getType());
                    }

                    values.add(value);
                    fields.add(field);

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access the field " + field.getName(), e);
                }
            }
        }

        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (").append("?,".repeat(values.size()));
        sql.setLength(sql.length() - 1);
        sql.append(");");

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
        ) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Column.class)) {
                            Column column = field.getAnnotation(Column.class);
                            if (column.autoIncrement()) {
                                field.setAccessible(true);
                                field.set(object, rs.getObject(1));
                            }
                        }
                    }
                }
            }

        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("An error occurred while trying to insert the object", e);
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public void update(T object) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        Field idField = getIdField(clazz);
        idField.setAccessible(true);

        try {
            Object idValue = idField.get(object);
            if (idValue == null) {
                throw new IllegalArgumentException("Cannot update object without an ID!");
            }

            StringBuilder sql = new StringBuilder("UPDATE " + table.name() + " SET ");
            List<Object> values = new ArrayList<>();

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    if (column.autoIncrement() || column.primary()) continue; // Não atualizar campos de ID ou Auto Increment

                    sql.append(column.name()).append(" = ?, ");
                    field.setAccessible(true);
                    values.add(field.get(object));
                }
            }

            sql.setLength(sql.length() - 2); // Removendo a última vírgula
            sql.append(" WHERE ").append(idField.getAnnotation(Column.class).name()).append(" = ?");

            values.add(idValue);

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }

                stmt.executeUpdate();
            }
        } catch (IllegalAccessException | SQLException e) {
            throw new RuntimeException("An error occurred while updating the object", e);
        }
    }

    @Override
    public void delete(T object) {

    }

    @Override
    public Optional<T> find(String column, Object value) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);

        String sql = "SELECT * FROM " + table.name() + " WHERE " + column + " = ?";


        long start = System.currentTimeMillis();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {

            stmt.setObject(1, value);
            ResultSet rs = stmt.executeQuery(); // TODO: add a try-with-resources

            if (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        Column columnAnnotation = field.getAnnotation(Column.class);
                        field.setAccessible(true);
                        field.set(obj, rs.getObject(columnAnnotation.name()));
                    }
                }

                System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
                return Optional.of(obj);
            }

        }catch (SQLException e) {
            throw new RuntimeException("An error occurred while trying to find the object", e);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Time: " + (System.currentTimeMillis() - start) + "ms");
        return Optional.empty();
    }

    @Override
    public Optional<T> find(String column, Object value, String... columns) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);

        StringBuilder sql = new StringBuilder("SELECT ");
        if (columns.length == 0) {
            sql.append("*");
        } else {
            for (String col : columns) {
                sql.append(col).append(", ");
            }
            sql.setLength(sql.length() - 2);
        }

        sql.append(" FROM ").append(table.name()).append(" WHERE ").append(column).append(" = ?");
        System.out.println(sql);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setObject(1, value);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();
                    // Criamos um Set para verificar se a coluna está na lista passada
                    Set<String> selectedColumns = new HashSet<>(Arrays.asList(columns));

                    for (Field field : clazz.getDeclaredFields()) {
                        if (field.isAnnotationPresent(Column.class)) {
                            Column columnAnnotation = field.getAnnotation(Column.class);
                            String columnName = columnAnnotation.name();

                            // Só preenche se estiver na lista de colunas ou se for um SELECT *
                            if (columns.length == 0 || selectedColumns.contains(columnName)) {
                                field.setAccessible(true);
                                field.set(obj, rs.getObject(columnName));
                            }
                        }
                    }
                    return Optional.of(obj);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("An error occurred while trying to find the object", e);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return Optional.empty();
    }

    @Override
    public List<T> findAll() {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT * FROM " + table.name();

        List<T> objects = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        Column columnAnnotation = field.getAnnotation(Column.class);
                        field.setAccessible(true);
                        field.set(obj, rs.getObject(columnAnnotation.name()));
                    }
                }
                objects.add(obj);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("An error occurred while trying to find all objects", e);
        }

        return objects;
    }

    @Override
    public List<T> findAll(String... columns) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        StringBuilder sql = new StringBuilder("SELECT ");
        if (columns.length == 0) {
            sql.append("*");
        } else {
            for (String col : columns) {
                sql.append(col).append(", ");
            }
            sql.setLength(sql.length() - 2);
        }

        sql.append(" FROM ").append(table.name());

        List<T> objects = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString());
             ResultSet rs = stmt.executeQuery();
        ) {
            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                // Criamos um Set para verificar se a coluna está na lista passada
                Set<String> selectedColumns = new HashSet<>(Arrays.asList(columns));

                for (Field field : clazz.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Column.class)) {
                        Column columnAnnotation = field.getAnnotation(Column.class);
                        String columnName = columnAnnotation.name();

                        // Só preenche se estiver na lista de colunas ou se for um SELECT *
                        if (columns.length == 0 || selectedColumns.contains(columnName)) {
                            field.setAccessible(true);
                            field.set(obj, rs.getObject(columnName));
                        }
                    }
                }
                objects.add(obj);
            }
        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("An error occurred while trying to find all objects", e);
        }

        return objects;

    }

    @Override
    public long count() {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT COUNT(*) FROM " + table.name();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery();
        ) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("An error occurred while trying to count the objects", e);
        }

        return 0;
    }

    @Override
    public long count(String column, Object value) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("The class " + clazz.getName() + " is not annotated with @Table annotation");
        }

        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT COUNT(*) FROM " + table.name() + " WHERE " + column + " = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
        ) {
            stmt.setObject(1, value);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("An error occurred while trying to count the objects", e);
        }

        return 0;
    }

    private Object parseDefaultValue(String defaultValue, Class<?> fieldType) {
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue) && fieldType == Timestamp.class) {
            return new Timestamp(System.currentTimeMillis());
        }
        if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(defaultValue);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(defaultValue);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        }
        return defaultValue;
    }

    private Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).primary()) {
                return field;
            }
        }
        throw new IllegalStateException("No primary key found in class " + clazz.getName());
    }

    private boolean recordExists(String columnName, Object value) {
        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT COUNT(*) FROM " + table.name() + " WHERE " + columnName + " = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking record existence", e);
        }
    }

}
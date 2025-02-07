package com.github.extraplays.ZenORM.database.impl;

import com.github.extraplays.ZenORM.database.DatabaseManager;
import com.github.extraplays.ZenORM.database.annotations.Id;
import com.github.extraplays.ZenORM.database.annotations.Table;
import com.github.extraplays.ZenORM.database.annotations.Default;
import com.github.extraplays.ZenORM.database.interfaces.ORM;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Processor<T> implements ORM<T> {

    private final Class<T> clazz;

    public Processor(Class<T> clazz) {
        this.clazz = clazz;
    }

    /**
     * Salva a entidade no banco de dados
     * @param entity entidade a ser salva
     */
    @Override
    public void save(T entity) {

        if (getIdValue(entity) == 0) {
            insert(entity);
        } else {
            update(entity);
        }

    }

    /**
     * Busca uma entidade no banco de dados
     * @param column coluna a ser buscada
     * @param value valor a ser buscado
     * @return entidade encontrada
     */
    @Override
    public Optional<T> findWhere(String column, Object value) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);

        if (!isValidColumn(column)) {
            throw new IllegalArgumentException("Coluna '" + column + "' não encontrada na classe " + clazz.getSimpleName());
        }

        String sql = "SELECT * FROM " + table.name() + " WHERE " + column + " = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) { // Fechando ResultSet corretamente
                if (rs.next()) {
                    T obj = clazz.getDeclaredConstructor().newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        field.setAccessible(true);
                        Object fieldValue = getValueFromResultSet(rs, field);
                        field.set(obj, fieldValue);
                    }
                    return Optional.of(obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // Melhor debug para encontrar o erro
            throw new RuntimeException("Erro ao buscar entidade!", e);
        }

        return Optional.empty();
    }

    /**
     * Insere uma entidade no banco de dados
     * @param entity entidade a ser inserida
     */
    @Override
    public void insert(T entity) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);
        StringBuilder sql = new StringBuilder("INSERT INTO " + table.name() + " (");

        Field[] fields = clazz.getDeclaredFields();
        List<Object> values = new ArrayList<>();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(Id.class)) {
                sql.append(field.getName()).append(", ");
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);

                    // 🚀 Se o valor for nulo e houver @Default, aplica o valor padrão
                    if (value == null && field.isAnnotationPresent(Default.class)) {
                        Default defaultAnnotation = field.getAnnotation(Default.class);
                        value = parseDefaultValue(defaultAnnotation.value(), field.getType());
                    }

                    values.add(value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Erro ao acessar o campo: " + field.getName(), e);
                }
            }
        }

        sql.setLength(sql.length() - 2);
        sql.append(") VALUES (").append("?,".repeat(values.size()));
        sql.setLength(sql.length() - 1);
        sql.append(");");

        System.out.println("[SQL] " + sql);

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir entidade!", e);
        }
    }

    /**
     * Busca uma entidade no banco de dados
     * @param id id da entidade
     * @return entidade encontrada
     */
    @Override
    public T findOne(int id) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT * FROM " + table.name() + " WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = getValueFromResultSet(rs, field);
                    field.set(obj, value);
                }
                return obj;
            }
        }catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Erro ao buscar entidade!", e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    /**
     * Busca todas as entidades no banco de dados
     * @return lista de entidades encontradas
     */
    @Override
    public List<T> findAll() {
        List<T> list = new ArrayList<>();

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);
        String sql = "SELECT * FROM " + table.name();

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object value = getValueFromResultSet(rs, field);
                    field.set(obj, value);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Deleta uma entidade do banco de dados
     * @param entity entidade a ser deletada
     */
    @Override
    public void delete(T entity) {

        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);
        int id = getIdValue(entity);

        if (id == 0) {
            throw new IllegalArgumentException("Não é possível deletar: ID não encontrado na entidade!");
        }

        String sql = "DELETE FROM " + table.name() + " WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar entidade!", e);
        }
    }

    /**
     * Atualiza uma entidade no banco de dados
     * @param entity entidade a ser atualizada
     */
    @Override
    public void update(T entity) {
        if (!clazz.isAnnotationPresent(Table.class)) {
            throw new IllegalArgumentException("Classe não anotada como @Table!");
        }

        Table table = clazz.getAnnotation(Table.class);
        int id = getIdValue(entity);

        if (id == 0) {
            throw new IllegalArgumentException("Não é possível atualizar: ID não encontrado na entidade!");
        }

        StringBuilder sql = new StringBuilder("UPDATE " + table.name() + " SET ");

        List<Object> values = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Id.class)) {
                sql.append(field.getName()).append(" = ?, ");
                field.setAccessible(true);
                try {
                    values.add(field.get(entity));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Não foi possível acessar o campo " + field.getName(), e);
                }
            }
        }

        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.setInt(values.size() + 1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar entidade!", e);
        }
    }

    private Object getValueFromResultSet(ResultSet rs, Field field) throws SQLException {
        String columnName = field.getName();
        if (field.getType() == int.class) return rs.getInt(columnName);
        if (field.getType() == long.class) return rs.getLong(columnName);
        if (field.getType() == double.class) return rs.getDouble(columnName);
        if (field.getType() == float.class) return rs.getFloat(columnName);
        if (field.getType() == boolean.class) return rs.getBoolean(columnName);
        if (field.getType() == String.class) return rs.getString(columnName);
        if (field.getType() == java.sql.Date.class) return rs.getDate(columnName);
        if (field.getType() == java.sql.Timestamp.class) return rs.getTimestamp(columnName);
        return null;
    }

    private int getIdValue(T entity) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    return field.getInt(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Erro ao acessar o ID do objeto!", e);
                }
            }
        }
        throw new IllegalStateException("Nenhuma coluna @Id encontrada na classe " + clazz.getSimpleName());
    }

    private boolean isValidColumn(String column) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equalsIgnoreCase(column)) {
                return true;
            }
        }
        return false;
    }

    private Object parseDefaultValue(String defaultValue, Class<?> fieldType) {
        if ("CURRENT_TIMESTAMP".equalsIgnoreCase(defaultValue) && fieldType == Timestamp.class) {
            return new Timestamp(System.currentTimeMillis());
        }

        if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(defaultValue);
        } else if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(defaultValue);
        } else if (fieldType == double.class || fieldType == Double.class) {
            return Double.parseDouble(defaultValue);
        } else if (fieldType == float.class || fieldType == Float.class) {
            return Float.parseFloat(defaultValue);
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        } else {
            return defaultValue; // Assume String como padrão
        }
    }


}

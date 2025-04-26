package com.github.extraplys.zenorm.entity;

import com.github.extraplys.zenorm.OrmManager;
import com.github.extraplys.zenorm.annotations.Column;
import com.github.extraplys.zenorm.annotations.Embedded;
import com.github.extraplys.zenorm.annotations.OneToMany;
import com.github.extraplys.zenorm.repository.AsyncRepository;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ResultSetMapper {

    @SuppressWarnings("unchecked")
    public static <T> T mapResultSet(Class<T> clazz, ResultSet rs) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    Object value = rs.getObject(column.name());

                    if (value != null) {
                        if (field.getType().isEnum()) {
                            field.set(instance, Enum.valueOf((Class<Enum>) field.getType(), value.toString()));
                        } else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class)) {
                            field.set(instance, rs.getBoolean(column.name()));
                        } else {
                            field.set(instance, value);
                        }
                    }
                }

                if (field.isAnnotationPresent(Embedded.class)) {
                    Object embeddedInstance = field.getType().getDeclaredConstructor().newInstance();

                    var metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    List<String> availableColumns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        availableColumns.add(metaData.getColumnName(i));
                    }

                    for (Field embeddedField : embeddedInstance.getClass().getDeclaredFields()) {
                        embeddedField.setAccessible(true);

                        if (embeddedField.isAnnotationPresent(Column.class)) {
                            Column embeddedColumn = embeddedField.getAnnotation(Column.class);

                            String fullColumnName = field.getName() + "_" + embeddedColumn.name();

                            if (availableColumns.contains(fullColumnName)) {
                                Object value = rs.getObject(fullColumnName);

                                if (value != null) {
                                    embeddedField.set(embeddedInstance, value);
                                }
                            }
                        }
                    }

                    field.set(instance, embeddedInstance);
                }

                if (field.isAnnotationPresent(OneToMany.class)) {
                    OneToMany oneToMany = field.getAnnotation(OneToMany.class);

                    Class<?> targetEntity = oneToMany.targetEntity();
                    String mappedBy = oneToMany.mappedBy();

                    AsyncRepository<?> repository = OrmManager.getInstance().getRepository(targetEntity);

                    Field primaryKeyField = TableUtils.getPrimaryKeyField(clazz);
                    primaryKeyField.setAccessible(true);
                    Object idValue = primaryKeyField.get(instance);

                    Field mappedByField = targetEntity.getDeclaredField(mappedBy);
                    mappedByField.setAccessible(true);

                    if (!mappedByField.isAnnotationPresent(Column.class)) {
                        throw new RuntimeException("Field " + mappedBy + " in " + targetEntity.getSimpleName() + " does not have @Column annotation!");
                    }

                    Column mappedByColumn = mappedByField.getAnnotation(Column.class);
                    String mappedByColumnName = mappedByColumn.name(); // nome da coluna real

                    List<?> children = repository.findManyByCondition(mappedByColumnName + " = ?", idValue).join();

                    field.set(instance, children != null ? children : new ArrayList<>());
                }
            }

            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to map ResultSet to entity: " + clazz.getName(), e);
        }
    }
}
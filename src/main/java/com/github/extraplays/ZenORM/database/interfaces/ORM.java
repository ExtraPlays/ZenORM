package com.github.extraplays.ZenORM.database.interfaces;

import java.util.List;
import java.util.Optional;

public interface ORM<T> {

    void save(T entity);
    void insert(T entity);
    T findOne(int id);
    Optional<T> findWhere(String column, Object value);
    List<T> findAll();
    void delete(T entity);
    void update(T entity);
}

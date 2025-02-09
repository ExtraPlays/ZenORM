package com.github.extraplays.ZenORM.database.interfaces;

import java.util.List;
import java.util.Optional;

public interface ORM<T> {

    /**
     * Save the object in the database
     * @param object Object to save
     */
    void save(T object);

    /**
     * Insert the object in the database
     * @param object Object to insert
     */
    void insert(T object);

    /**
     * Update the object in the database
     * @param object Object to update
     */
    void update(T object);

    /**
     * Delete the object in the database
     * @param object Object to delete
     */
    void delete(T object);

    /**
     * Find the object in the database
     * @param column Column to search
     * @param value Value to search
     * @return Object found
     */
    Optional<T> find(String column, Object value);

    /**
     * Find the object in the database
     * @param column Column to search
     * @param value Value to search
     * @param columns Columns to return
     * @return Object found
     */
    Optional<T> find(String column, Object value, String... columns);

    /**
     * Find all objects in the database
     * @return Objects found
     */
    List<T> findAll();

    /**
     * Find all objects in the database
     * @param columns Columns to return
     * @return Objects found
     */
    List<T> findAll(String... columns);

    /**
     * Count all objects in the database
     * @return Objects count
     */
    long count();

    /**
     * Count all objects in the database
     * @param column Column to count
     * @param value Value to count
     * @return Objects count
     */
    long count(String column, Object value);

}

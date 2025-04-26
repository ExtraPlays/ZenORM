package com.github.extraplays.zenorm.query;

import com.github.extraplays.zenorm.entity.EntityMapper;
import com.github.extraplays.zenorm.entity.TableUtils;
import com.github.extraplays.zenorm.providers.DatabaseProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QueryBuilder<T> {

    private final Class<T> entityClass;
    private final DatabaseProvider provider;

    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();

    private String orderBy = null;
    private Integer limit = null;

    public QueryBuilder(Class<T> entityClass, DatabaseProvider provider) {
        this.entityClass = entityClass;
        this.provider = provider;
    }

    public QueryBuilder<T> where(String column, String operator, Object value) {

        if (operator == null || operator.isEmpty()) {
            throw new IllegalArgumentException("Operator cannot be null or empty");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        switch (operator) {
            case "=":
            case "!=":
            case ">":
            case "<":
            case ">=":
            case "<=":
            case "LIKE":
                break;
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }

        conditions.add(column + " " + operator + " ?");
        parameters.add(value);
        return this;
    }

    public QueryBuilder<T> and(String column, String operator, Object value) {
        return where(column, operator, value);
    }

    public QueryBuilder<T> orderBy(String column, String direction) {
        this.orderBy = column + " " + direction;
        return this;
    }

    public QueryBuilder<T> limit(int limit) {
        this.limit = limit;
        return this;
    }

    public List<T> findAll() {
        return EntityMapper.findManyByQuery(entityClass, provider, buildQuery(), parameters.toArray());
    }

    public Optional<T> findOne() {
        this.limit = 1;
        List<T> results = findAll();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    private String buildQuery() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(TableUtils.getTableName(entityClass));

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        if (orderBy != null) {
            sql.append(" ORDER BY ").append(orderBy);
        }

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }

        return sql.toString();
    }

}

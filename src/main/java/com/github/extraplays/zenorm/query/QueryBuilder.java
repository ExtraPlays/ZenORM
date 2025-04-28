package com.github.extraplays.zenorm.query;

import com.github.extraplays.zenorm.entity.EntityMapper;
import com.github.extraplays.zenorm.entity.TableUtils;
import com.github.extraplays.zenorm.providers.DatabaseProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class QueryBuilder<T> {

    private final Class<T> entityClass;
    private final DatabaseProvider provider;

    private final List<String> conditions = new ArrayList<>();
    private final List<Object> parameters = new ArrayList<>();
    private final List<String> orderByFields = new ArrayList<>();
    private final List<String> selectFields = new ArrayList<>();

    private Integer offset = null;
    private Integer limit = null;

    public QueryBuilder(Class<T> entityClass, DatabaseProvider provider) {
        this.entityClass = entityClass;
        this.provider = provider;
    }

    public QueryBuilder<T> select(String... fields) {
        selectFields.clear();
        Collections.addAll(selectFields, fields);
        return this;
    }

    public QueryBuilder<T> where(String column, String operator, Object value) {
        validateOperator(operator);
        conditions.add(column + " " + operator + " ?");
        parameters.add(value);
        return this;
    }

    private void validateOperator(String operator) {
        switch (operator) {
            case "=", "!=", ">", "<", ">=", "<=", "LIKE":
                break;
            default:
                throw new IllegalArgumentException("Invalid SQL operator: " + operator);
        }
    }

    public QueryBuilder<T> and(String column, String operator, Object value) {
        return where(column, operator, value);
    }

    public QueryBuilder<T> or(String column, String operator, Object value) {
        if (conditions.isEmpty()) {
            return where(column, operator, value);
        }
        validateOperator(operator);
        conditions.add("OR " + column + " " + operator + " ?");
        parameters.add(value);
        return this;
    }

    public QueryBuilder<T> orderBy(String column, String direction) {
        orderByFields.add(column + " " + direction);
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

        sql.append("SELECT ");

        if (!selectFields.isEmpty()) {
            sql.append(String.join(", ", selectFields));
        } else {
            sql.append("*");
        }

        sql.append(" FROM ").append(TableUtils.getTableName(entityClass));

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            sql.append(String.join(" ", conditions));
        }

        if (!orderByFields.isEmpty()) {
            sql.append(" ORDER BY ");
            sql.append(String.join(", ", orderByFields));
        }

        if (limit != null) {
            sql.append(" LIMIT ").append(limit);
        }

        if (offset != null) {
            sql.append(" OFFSET ").append(offset);
        }

        return sql.toString();
    }

}

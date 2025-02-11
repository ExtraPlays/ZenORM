package com.github.extraplays.ZenORM.database.impl.async;

import com.github.extraplays.ZenORM.database.impl.Processor;
import com.zaxxer.hikari.HikariDataSource;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class AsyncProcessor<T> extends Processor<T> {

    public AsyncProcessor(Class<T> clazz, HikariDataSource dataSource) {
        super(clazz, dataSource);
    }

    public CompletableFuture<Void> saveAsync(T object) {
        return CompletableFuture.runAsync(() -> save(object), AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Void> insertAsync(T object) {
        return CompletableFuture.runAsync(() -> insert(object), AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Void> updateAsync(T object) {
        return CompletableFuture.runAsync(() -> update(object), AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Void> deleteAsync(T object) {
        return CompletableFuture.runAsync(() -> delete(object), AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Optional<T>> findAsync(String column, Object value) {
        return CompletableFuture.supplyAsync(() -> find(column, value), AsyncExecutor.getExecutor());
    }

    public CompletableFuture<List<T>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAll, AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Long> countAsync() {
        return CompletableFuture.supplyAsync(this::count, AsyncExecutor.getExecutor());
    }

    public CompletableFuture<Long> countAsync(String column, Object value) {
        return CompletableFuture.supplyAsync(() -> count(column, value), AsyncExecutor.getExecutor());
    }

}

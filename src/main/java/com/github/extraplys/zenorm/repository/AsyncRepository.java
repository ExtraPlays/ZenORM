package com.github.extraplys.zenorm.repository;

import com.github.extraplys.zenorm.entity.EntityMapper;
import com.github.extraplys.zenorm.providers.DatabaseProvider;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class AsyncRepository<T> {


    private final Class<T> entityClass;
    private final DatabaseProvider provider;
    private final ExecutorService executor;

    public CompletableFuture<Void> saveAsync(T entity) {
        return CompletableFuture.runAsync(() -> {
            EntityMapper.save(entity, provider);
        }, executor);
    }

    public CompletableFuture<Void> updateAsync(T entity) {
        return CompletableFuture.runAsync(() -> {
            EntityMapper.update(entity, provider);
        }, executor);
    }

    public CompletableFuture<Void> deleteAsync(T entity) {
        return CompletableFuture.runAsync(() -> {
            EntityMapper.delete(entity, provider);
        }, executor);
    }

    public CompletableFuture<Optional<T>> findByIdAsync(Object id) {
        return CompletableFuture.supplyAsync(() ->
                EntityMapper.findById(entityClass, provider, id)
            , executor);
    }

    public CompletableFuture<List<T>> findAllAsync() {
        return CompletableFuture.supplyAsync(() ->
            EntityMapper.findAll(entityClass, provider)
        , executor);
    }

    public CompletableFuture<List<T>> findManyByCondition(String condition, Object... params) {
        return CompletableFuture.supplyAsync(() ->
                EntityMapper.findManyByCondition(entityClass, provider, condition, params),
            executor
        );
    }

    public CompletableFuture<Optional<T>> findOneAsync(String query, Object... params) {
        return CompletableFuture.supplyAsync(() ->
            EntityMapper.findOne(entityClass, provider, query, params)
        , executor);
    }

}

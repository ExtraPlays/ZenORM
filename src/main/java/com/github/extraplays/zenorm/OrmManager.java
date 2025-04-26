package com.github.extraplays.zenorm;

import com.github.extraplays.zenorm.providers.DatabaseProvider;
import com.github.extraplays.zenorm.query.QueryBuilder;
import com.github.extraplays.zenorm.repository.AsyncRepository;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrmManager {

    @Getter
    private static OrmManager instance;
    @Getter
    private final DatabaseProvider provider;

    private final ExecutorService executorService;
    private final Map<Class<?>, AsyncRepository<?>> repositories;

    public OrmManager(DatabaseProvider provider) {
        if (instance != null) {
            throw new IllegalStateException("OrmManager is already initialized");
        }

        if (provider == null) {
            throw new IllegalArgumentException("DatabaseProvider cannot be null");
        }

        this.executorService = Executors.newCachedThreadPool();
        this.provider = provider;
        this.repositories = new HashMap<>();
        instance = this;
    }

    public <T> AsyncRepository<T> getRepository(Class<T> entityClass) {
        if (repositories.containsKey(entityClass)) {
            return (AsyncRepository<T>) repositories.get(entityClass);
        }

        AsyncRepository<T> repository = new AsyncRepository<>(entityClass, provider, executorService);

        repositories.put(entityClass, repository);
        return repository;
    }

    public <T> QueryBuilder<T> query(Class<T> entityClass) {
        return new QueryBuilder<>(entityClass, provider);
    }

    public void shutdown() {
        executorService.shutdownNow();
        provider.shutdown();
    }

}

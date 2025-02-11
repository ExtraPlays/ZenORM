package com.github.extraplays.ZenORM.database.impl.cache;

public class CacheEntry<T> {

    T data;
    long timestamp;

    CacheEntry(T data, long timestamp) {
        this.data = data;
        this.timestamp = timestamp;
    }

}

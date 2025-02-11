package com.github.extraplays.ZenORM.database.impl.async;

import lombok.Getter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncExecutor {

    @Getter
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

}

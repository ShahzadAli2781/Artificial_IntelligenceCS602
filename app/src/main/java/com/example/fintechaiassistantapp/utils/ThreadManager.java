package com.example.fintechaiassistantapp.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public static void runInBackground(Runnable runnable) {
        executorService.execute(runnable);
    }
}
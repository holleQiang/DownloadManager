package com.zhangqiang.downloadmanager.manager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorManager {

    private static final ExecutorManager instance = new ExecutorManager();
    private final ExecutorService executor = new ThreadPoolExecutor(4,
            20,
            10 * 1000,
            TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "download_manager_thread");
                }
            });

    private ExecutorManager() {
    }

    public static ExecutorManager getInstance() {
        return instance;
    }

    public Future<?> submit(Runnable runnable) {
        return executor.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable){
        return executor.submit(callable);
    }
}

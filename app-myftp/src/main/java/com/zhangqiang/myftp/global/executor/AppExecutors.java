package com.zhangqiang.myftp.global.executor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    public static final Executor defaultExecutor = Executors.newFixedThreadPool(5);
}

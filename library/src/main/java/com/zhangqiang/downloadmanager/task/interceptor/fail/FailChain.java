package com.zhangqiang.downloadmanager.task.interceptor.fail;

public abstract class FailChain {

    private final Throwable throwable;

    public FailChain(Throwable e) {
        this.throwable = e;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public abstract void proceed(Throwable e);
}

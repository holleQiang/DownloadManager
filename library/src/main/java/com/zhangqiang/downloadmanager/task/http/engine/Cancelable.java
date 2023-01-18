package com.zhangqiang.downloadmanager.task.http.engine;

public interface Cancelable {

    void cancel() throws RuntimeException;

    boolean isCancelled();
}

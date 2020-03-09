package com.zhangqiang.downloadmanager.task.part;

public interface Callback {

    void onStart();

    void onProgress(long current, long start, long end);

    void onComplete();

    void onFail(Throwable e);

    void onPause();
}

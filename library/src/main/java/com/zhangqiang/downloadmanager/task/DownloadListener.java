package com.zhangqiang.downloadmanager.task;

public interface DownloadListener {

    void onStart();

    void onProgress(long current,long total);

    void onPause();

    void onFail(Throwable e);

    void onComplete();

}

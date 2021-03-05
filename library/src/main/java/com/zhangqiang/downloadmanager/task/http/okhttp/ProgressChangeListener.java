package com.zhangqiang.downloadmanager.task.http.okhttp;

public abstract class ProgressChangeListener implements OKHttpDownloadTask.OnProgressChangeListener{

    @Override
    public void onProgressChange(int threadIndex, int threadSize, long current, long start, long end, long total) {

    }
}

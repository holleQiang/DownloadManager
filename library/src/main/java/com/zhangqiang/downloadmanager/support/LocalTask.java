package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.task.DownloadTask;

public class LocalTask {

    private final String id;
   private final DownloadTask downloadTask;
   private final boolean isRunning;

    public LocalTask(String id, DownloadTask downloadTask, boolean isRunning) {
        this.id = id;
        this.downloadTask = downloadTask;
        this.isRunning = isRunning;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public String getId() {
        return id;
    }
}

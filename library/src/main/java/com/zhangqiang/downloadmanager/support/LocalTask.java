package com.zhangqiang.downloadmanager.support;

public class LocalTask {

    private final String id;
    private final DownloadBundle downloadBundle;
    private final boolean isRunning;

    public LocalTask(String id, DownloadBundle downloadBundle, boolean isRunning) {
        this.id = id;
        this.downloadBundle = downloadBundle;
        this.isRunning = isRunning;
    }

    public DownloadBundle getDownloadBundle() {
        return downloadBundle;
    }

    public boolean isRunning() {
        return isRunning;
    }


    public String getId() {
        return id;
    }
}

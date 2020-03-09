package com.zhangqiang.downloadmanager.task;

public abstract class Downloader {

    private boolean running;

    protected abstract void onStart(DownloadTask downloadTask);

    protected abstract void onStop();

    boolean isStop() {
        return !running;
    }

    void stop() {
        this.running = false;
        onStop();
    }

    public void start(DownloadTask downloadTask) {
        running = true;
        try {
            onStart(downloadTask);
        } finally {
            running = false;
        }
    }
}

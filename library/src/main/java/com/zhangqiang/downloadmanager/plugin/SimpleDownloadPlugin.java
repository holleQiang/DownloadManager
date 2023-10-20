package com.zhangqiang.downloadmanager.plugin;

import com.zhangqiang.downloadmanager.manager.DownloadManager;

public class SimpleDownloadPlugin implements DownloadPlugin {

    private DownloadManager downloadManager;
    private boolean available;

    @Override
    public final void apply(DownloadManager downloadManager) {
        available = true;
        this.downloadManager = downloadManager;
        onApply(downloadManager);
    }

    protected void onApply(DownloadManager downloadManager) {

    }

    @Override
    public final void drop() {
        onDrop(downloadManager);
        available = false;
        this.downloadManager = null;
    }

    protected void onDrop(DownloadManager downloadManager) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescribe() {
        return null;
    }

    public DownloadManager getDownloadManager() {
        return downloadManager;
    }

    public boolean isAvailable() {
        return available;
    }
}

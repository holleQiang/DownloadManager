package com.zhangqiang.downloadmanager.plugin;

import com.zhangqiang.downloadmanager.manager.DownloadManager;

public class SimpleDownloadPlugin implements DownloadPlugin {

    private DownloadManager downloadManager;

    @Override
    public void apply(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }

    @Override
    public void drop(DownloadManager downloadManager) {
        this.downloadManager = null;
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
}

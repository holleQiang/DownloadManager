package com.zhangqiang.downloadmanager2.plugin;

import com.zhangqiang.downloadmanager2.manager.DownloadManager;

public interface DownloadPlugin {

    void apply(DownloadManager downloadManager);

    void drop();

    String getName();
}

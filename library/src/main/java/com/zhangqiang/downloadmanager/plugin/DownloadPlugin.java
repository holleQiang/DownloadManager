package com.zhangqiang.downloadmanager.plugin;

import com.zhangqiang.downloadmanager.manager.DownloadManager;

public interface DownloadPlugin {

    void apply(DownloadManager downloadManager);

    void drop();

    String getName();

    String getDescribe();
}

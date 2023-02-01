package com.zhangqiang.downloadmanager.listeners;

public interface DownloadTaskListener {

    void onTaskAdded(String id);

    void onTaskRemoved(String id);

    void onActiveTaskSizeChanged();
}

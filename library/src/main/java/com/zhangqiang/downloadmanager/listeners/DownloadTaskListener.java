package com.zhangqiang.downloadmanager.listeners;

public interface DownloadTaskListener {

    void onTaskAdded(String id);

    void onTaskRemoved(String id);

    void onTaskStateChanged(String id);

    void onTaskInfoChanged(String id);

    void onTaskProgressChanged(String id);

    void onTaskSpeedChanged(String id);

    void onActiveTaskSizeChanged();
}

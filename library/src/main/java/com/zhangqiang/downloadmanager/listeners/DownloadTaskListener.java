package com.zhangqiang.downloadmanager.listeners;

public interface DownloadTaskListener {

    void onTaskAdded(long id);

    void onTaskRemoved(long id);

    void onTaskStateChanged(long id);

    void onTaskInfoChanged(long id);

    void onTaskProgressChanged(long id);

    void onTaskSpeedChanged(long id);
}

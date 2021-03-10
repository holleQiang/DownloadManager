package com.zhangqiang.downloadmanager.listener;

public interface DownloadTaskListener {

    void onTaskAdded(long id);

    void onTaskRemoved(long id);

    void onTaskStateChanged(long id);

    void onTaskInfoChanged(long id);

    void onTaskProgressChanged(long id);

    void onTaskSpeedChanged(long id);
}

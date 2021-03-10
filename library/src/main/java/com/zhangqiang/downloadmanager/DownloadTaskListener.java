package com.zhangqiang.downloadmanager;

public interface DownloadTaskListener {

    void onTaskAdded(long id);

    void onTaskRemoved(long id);

    void onTaskStateChanged(long id);

    void onTaskInfoChanged(long id);

    void onTaskProgressChanged(long id);
}

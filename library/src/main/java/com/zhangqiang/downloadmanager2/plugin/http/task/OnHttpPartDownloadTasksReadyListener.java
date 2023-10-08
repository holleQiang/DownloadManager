package com.zhangqiang.downloadmanager2.plugin.http.task;

import java.util.List;

public interface OnHttpPartDownloadTasksReadyListener {

    void onHttpPartDownloadTasksReady(List<HttpPartDownloadTask> partDownloadTasks);
}

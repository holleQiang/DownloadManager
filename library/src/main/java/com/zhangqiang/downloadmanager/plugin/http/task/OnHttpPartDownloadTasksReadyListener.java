package com.zhangqiang.downloadmanager.plugin.http.task;

import java.util.List;

public interface OnHttpPartDownloadTasksReadyListener {

    void onHttpPartDownloadTasksReady(List<HttpPartDownloadTask> partDownloadTasks);
}

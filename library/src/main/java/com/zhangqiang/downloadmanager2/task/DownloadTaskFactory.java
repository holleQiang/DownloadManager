package com.zhangqiang.downloadmanager2.task;

import com.zhangqiang.downloadmanager2.request.DownloadRequest;

public interface DownloadTaskFactory {

    DownloadTask createTask(DownloadRequest downloadRequest);
}

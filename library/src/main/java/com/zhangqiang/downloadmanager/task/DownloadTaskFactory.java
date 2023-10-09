package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.request.DownloadRequest;

public interface DownloadTaskFactory {

    DownloadTask createTask(DownloadRequest downloadRequest);
}

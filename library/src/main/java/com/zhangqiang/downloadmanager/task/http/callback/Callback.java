package com.zhangqiang.downloadmanager.task.http.callback;

import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;

import java.util.List;

public interface Callback {

    void onStartGenerateInfo();

    void onResourceInfoReady(ResourceInfo info);

    void onStartDefaultDownload();

    void onStartPartDownload();

    void onPartTasksCreate(List<HttpDownloadPartTask> tasks);

    void onPartTaskFail(HttpDownloadPartTask task, Throwable e);
}

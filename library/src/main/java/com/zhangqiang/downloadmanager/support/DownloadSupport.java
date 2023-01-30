package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface DownloadSupport {

    List<DownloadTask> loadDownloadTasks();

    DownloadTask createDownloadTask(DownloadRequest request);

    TaskInfo buildTaskInfo(DownloadTask downloadTask);

    void handleSyncTaskProgress(DownloadTask downloadTask);

    boolean isTaskIdle(DownloadTask downloadTask);

    boolean isTaskRunning(DownloadTask downloadTask);

    void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile);
}

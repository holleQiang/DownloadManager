package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface DownloadSupport {

    List<DownloadTask> loadDownloadTasks();

    DownloadTask createDownloadTask(DownloadRequest request, String taskId);

    TaskInfo buildTaskInfo(DownloadTask downloadTask);

    void handleSyncTaskProgress(DownloadTask downloadTask);

    boolean isTaskIdle(DownloadTask downloadTask);

    boolean isTaskDownloading(DownloadTask downloadTask);

    void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile);
}

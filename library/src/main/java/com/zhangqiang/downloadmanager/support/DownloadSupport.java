package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface DownloadSupport {

    List<DownloadTask> loadDownloadTasks();

    boolean isTaskRunning(DownloadTask downloadTask);

    DownloadTask createDownloadTask(DownloadRequest request);

    TaskInfo buildTaskInfo(DownloadTask downloadTask);

    boolean handleProgressSync(DownloadTask downloadTask);

    boolean handleSpeedCompute(DownloadTask downloadTask);

    boolean isTaskIdle(DownloadTask downloadTask);

    void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile);
}

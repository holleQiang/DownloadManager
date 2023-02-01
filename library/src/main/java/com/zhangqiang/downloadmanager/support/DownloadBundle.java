package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

public class DownloadBundle {
    private final DownloadTask downloadTask;
    private final TaskInfo taskInfo;

    public DownloadBundle(DownloadTask downloadTask, TaskInfo taskInfo) {
        this.downloadTask = downloadTask;
        this.taskInfo = taskInfo;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public TaskInfo getTaskInfo() {
        return taskInfo;
    }
}

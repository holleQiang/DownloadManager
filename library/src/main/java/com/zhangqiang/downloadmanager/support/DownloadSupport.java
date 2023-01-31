package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface DownloadSupport {

    /**
     * load task from local
     */
    List<LocalTask> loadLocalTasks();

    /**
     * create download task by request
     */
    DownloadTask createDownloadTask(String id, DownloadRequest request);

    /**
     * create task info getter
     */
    TaskInfo buildTaskInfo(DownloadTask downloadTask);

    /**
     * sync progress info
     * @return notify progress update when true
     */
    boolean handleProgressSync(DownloadTask downloadTask);
    /**
     * compute speed of task
     * @return notify speed update when true
     */
    boolean handleSpeedCompute(DownloadTask downloadTask);

    /**
     * if task is idle and active task size is legal，task will be start
     * @return true is task is idle
     */
    boolean isTaskIdle(DownloadTask downloadTask);

    /**
     * task is remove from queue
     */
    void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile);
}

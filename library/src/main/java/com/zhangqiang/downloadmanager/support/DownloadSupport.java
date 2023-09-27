package com.zhangqiang.downloadmanager.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface DownloadSupport {

    boolean support(DownloadRequest request);

    /**
     * load task from local
     */
    List<LocalTask> loadLocalTasks();

    /**
     * create download task by request
     */
    DownloadBundle createDownloadBundle(String id, DownloadRequest request);

    /**
     * sync progress info
     * @return notify progress update when true
     */
    void handleProgressSync(DownloadBundle downloadBundle);
    /**
     * compute speed of task
     * @return notify speed update when true
     */
    void handleSpeedCompute(DownloadBundle downloadBundle);

    /**
     * if task is idle and active task size is legal，task will be start
     * @return true is task is idle
     */
    boolean isTaskIdle(DownloadBundle downloadBundle);

    /**
     * task is remove from queue
     */
    void handleDeleteTask(DownloadBundle downloadBundle, boolean deleteFile);
}

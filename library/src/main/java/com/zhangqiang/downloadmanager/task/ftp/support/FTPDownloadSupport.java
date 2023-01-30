package com.zhangqiang.downloadmanager.task.ftp.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;

import java.util.List;
import java.util.UUID;

public class FTPDownloadSupport implements DownloadSupport {

    @Override
    public List<DownloadTask> loadDownloadTasks() {
        return null;
    }

    @Override
    public DownloadTask createDownloadTask(DownloadRequest request) {
        return new FTPDownloadTask(UUID.randomUUID().toString());
    }

    @Override
    public TaskInfo buildTaskInfo(DownloadTask downloadTask) {
        return null;
    }

    @Override
    public boolean handleProgressSync(DownloadTask downloadTask) {
        return false;
    }

    @Override
    public boolean handleSpeedCompute(DownloadTask downloadTask) {
        return false;
    }

    @Override
    public boolean isTaskIdle(DownloadTask downloadTask) {
        return true;
    }

    @Override
    public boolean isTaskRunning(DownloadTask downloadTask) {
        return false;
    }

    @Override
    public void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile) {

    }
}

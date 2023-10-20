package com.zhangqiang.downloadmanager.manager;

import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public interface OnTaskAddedListener {

    void onTaskAdded(List<DownloadTask> tasks);
}

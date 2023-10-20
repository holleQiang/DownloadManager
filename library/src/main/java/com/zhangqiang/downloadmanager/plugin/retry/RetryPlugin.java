package com.zhangqiang.downloadmanager.plugin.retry;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.OnTaskAddedListener;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public class RetryPlugin extends SimpleDownloadPlugin {
    @Override
    public void apply(DownloadManager downloadManager) {
        downloadManager.addOnTaskAddedListener(onTaskAddedListener);
    }

    @Override
    public void drop(DownloadManager downloadManager) {
        downloadManager.removeOnTaskAddedListener(onTaskAddedListener);
    }

    @Override
    public String getName() {
        return "错误重试插件";
    }

    private final OnTaskAddedListener onTaskAddedListener = new OnTaskAddedListener() {
        @Override
        public void onTaskAdded(List<DownloadTask> tasks) {
            for (DownloadTask downloadTask : tasks) {
                downloadTask.addFailInterceptor(new RetryFailInterceptor(downloadTask));
            }
        }
    };
}

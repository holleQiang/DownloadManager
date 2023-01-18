package com.zhangqiang.downloadmanager.task.http.part;

import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;

public interface HttpPartTaskFactory {

    HttpDownloadPartTask onCreateHttpPartTask(String url, long start, long end, String filePath);
}

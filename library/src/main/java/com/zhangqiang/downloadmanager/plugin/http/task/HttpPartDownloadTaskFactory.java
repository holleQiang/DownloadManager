package com.zhangqiang.downloadmanager.plugin.http.task;

public interface HttpPartDownloadTaskFactory {

    HttpPartDownloadTask createHttpPartDownloadTask(String saveDir, String targetFileName, String url, long startPosition, long endPosition);
}

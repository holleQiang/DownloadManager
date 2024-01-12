package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;

public class TSDownloadBundle {

    private final TSInfo info;
    private final HttpPartDownloadTask downloadTask;

    public TSDownloadBundle(TSInfo info, HttpPartDownloadTask downloadTask) {
        this.info = info;
        this.downloadTask = downloadTask;
    }

    public TSInfo getInfo() {
        return info;
    }

    public HttpPartDownloadTask getDownloadTask() {
        return downloadTask;
    }
}

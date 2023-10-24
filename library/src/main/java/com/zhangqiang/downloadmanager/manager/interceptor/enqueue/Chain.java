package com.zhangqiang.downloadmanager.manager.interceptor.enqueue;

import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;

public abstract class Chain {

    private final DownloadRequest request;

    public Chain(DownloadRequest request) {
        this.request = request;
    }

    public DownloadRequest getRequest() {
        return request;
    }

    public abstract DownloadTask proceed(DownloadRequest request);
}

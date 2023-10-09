package com.zhangqiang.downloadmanager.plugin.http.request;

import com.zhangqiang.downloadmanager.request.DownloadRequest;

public class HttpDownloadRequest extends DownloadRequest {

    private final String url;
    private int threadSize;

    public HttpDownloadRequest(String saveDir, String targetFileName, String url, int threadSize) {
        super(saveDir, targetFileName);
        this.url = url;
        this.threadSize = threadSize;
    }

    public String getUrl() {
        return url;
    }

    public int getThreadSize() {
        return threadSize;
    }
}

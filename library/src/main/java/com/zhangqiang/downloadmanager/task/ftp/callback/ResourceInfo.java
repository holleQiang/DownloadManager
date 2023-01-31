package com.zhangqiang.downloadmanager.task.ftp.callback;

public class ResourceInfo {
    private long contentLength;

    public ResourceInfo(long contentLength) {
        this.contentLength = contentLength;
    }

    public long getContentLength() {
        return contentLength;
    }
}

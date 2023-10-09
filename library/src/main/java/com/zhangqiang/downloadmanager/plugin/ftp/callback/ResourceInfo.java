package com.zhangqiang.downloadmanager.plugin.ftp.callback;

public class ResourceInfo {
    private final long contentLength;
    private final String contentType;
    public ResourceInfo(long contentLength, String contentType) {
        this.contentLength = contentLength;
        this.contentType = contentType;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }
}

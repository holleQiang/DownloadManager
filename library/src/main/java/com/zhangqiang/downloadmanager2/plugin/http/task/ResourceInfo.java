package com.zhangqiang.downloadmanager2.plugin.http.task;

public class ResourceInfo {

    private final String fileName;
    private final long contentLength;
    private final String contentType;
    private final int responseCode;

    public ResourceInfo(String fileName, long contentLength, String contentType, int responseCode) {
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.responseCode = responseCode;
    }

    public String getFileName() {
        return fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                "fileName='" + fileName + '\'' +
                ", contentLength=" + contentLength +
                ", contentType='" + contentType + '\'' +
                ", responseCode=" + responseCode +
                '}';
    }
}

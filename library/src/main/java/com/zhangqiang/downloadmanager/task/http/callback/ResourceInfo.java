package com.zhangqiang.downloadmanager.task.http.callback;

public class ResourceInfo {

    private String fileName;
    private long contentLength;
    private String contentType;
    private String lastModified;
    private String eTag;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getETag() {
        return eTag;
    }

    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                "fileName='" + fileName + '\'' +
                ", contentLength=" + contentLength +
                ", contentType='" + contentType + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", eTag='" + eTag + '\'' +
                '}';
    }
}

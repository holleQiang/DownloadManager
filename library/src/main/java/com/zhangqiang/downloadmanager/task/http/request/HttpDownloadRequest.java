package com.zhangqiang.downloadmanager.task.http.request;

import com.zhangqiang.downloadmanager.DownloadRequest;

public class HttpDownloadRequest extends DownloadRequest {

    private final String url;
    private final int threadSize;

    HttpDownloadRequest(String saveDir, String fileName, String url, int threadSize) {
        super(saveDir, fileName);
        this.url = url;
        this.threadSize = threadSize;
    }

    public String getUrl() {
        return url;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public static class Builder {

        private String url;
        private int threadSize;
        private String saveDir;
        private String fileName;

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setThreadSize(int threadSize) {
            this.threadSize = threadSize;
            return this;
        }

        public Builder setSaveDir(String saveDir) {
            this.saveDir = saveDir;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public HttpDownloadRequest build() {
            return new HttpDownloadRequest(saveDir, fileName, url, threadSize);
        }
    }
}

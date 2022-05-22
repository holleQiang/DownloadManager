package com.zhangqiang.downloadmanager;

public class DownloadRequest {

    private final String url;
    private final int threadSize;
    private final String saveDir;
    private final String fileName;

    DownloadRequest(String url, int threadSize, String saveDir, String fileName) {
        this.url = url;
        this.threadSize = threadSize;
        this.saveDir = saveDir;
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getFileName() {
        return fileName;
    }

    public static class Builder {

        private final String url;
        private int threadSize;
        private final String saveDir;
        private String fileName;

        public Builder(String url, String saveDir) {
            this.url = url;
            this.saveDir = saveDir;
            this.threadSize = 1;
        }

        public Builder setThreadSize(int threadSize) {
            this.threadSize = threadSize;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(url, threadSize, saveDir, fileName);
        }
    }

    public Builder newBuilder() {
        return new Builder(url, saveDir)
                .setFileName(fileName)
                .setThreadSize(threadSize);
    }
}

package com.zhangqiang.downloadmanager;

public class DownloadRequest {

    private final String url;
    private final int threadCount;
    private final String saveDir;
    private final String fileName;

    DownloadRequest(String url, int threadCount, String saveDir, String fileName) {
        this.url = url;
        this.threadCount = threadCount;
        this.saveDir = saveDir;
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getFileName() {
        return fileName;
    }

    public static class Builder {

        private final String url;
        private int threadCount;
        private final String saveDir;
        private String fileName;

        public Builder(String url, String saveDir) {
            this.url = url;
            this.saveDir = saveDir;
            this.threadCount = 1;
        }

        public Builder setThreadCount(int threadCount) {
            this.threadCount = Math.max(1, threadCount);
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(url, threadCount, saveDir, fileName);
        }
    }

    public Builder newBuilder() {
        return new Builder(url, saveDir)
                .setFileName(fileName)
                .setThreadCount(threadCount);
    }
}

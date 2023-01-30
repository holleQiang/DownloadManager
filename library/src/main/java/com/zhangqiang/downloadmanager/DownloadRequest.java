package com.zhangqiang.downloadmanager;

public abstract class DownloadRequest {

    private final String saveDir;
    private final String fileName;

    public DownloadRequest(String saveDir, String fileName) {
        this.saveDir = saveDir;
        this.fileName = fileName;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getFileName() {
        return fileName;
    }
}

package com.zhangqiang.downloadmanager2.request;

public class DownloadRequest {
    private final String saveDir;
    private final String targetFileName;

    public DownloadRequest(String saveDir, String targetFileName) {
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getTargetFileName() {
        return targetFileName;
    }
}

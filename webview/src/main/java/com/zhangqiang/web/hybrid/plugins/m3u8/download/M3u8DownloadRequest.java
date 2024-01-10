package com.zhangqiang.web.hybrid.plugins.m3u8.download;

import com.zhangqiang.downloadmanager.request.DownloadRequest;

public class M3u8DownloadRequest extends DownloadRequest {

    private final String url;

    public M3u8DownloadRequest(String saveDir, String targetFileName, String url) {
        super(saveDir, targetFileName);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

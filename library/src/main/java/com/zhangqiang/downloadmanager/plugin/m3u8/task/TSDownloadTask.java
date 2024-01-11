package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import android.content.Context;

import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.ResourceInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;
import com.zhangqiang.downloadmanager.task.Status;

public class TSDownloadTask extends HttpPartDownloadTask {

    private final TSInfo info;

    public TSDownloadTask(String id, String saveDir, String targetFileName, long createTime, String url, Context context, long startPosition, long endPosition, TSInfo info) {
        super(id, saveDir, targetFileName, createTime, url, context, startPosition, endPosition);
        this.info = info;
    }

    public TSDownloadTask(String id, String saveDir, String targetFileName, long createTime, Status status, String errorMessage, String url, ResourceInfo resourceInfo, long currentLength, Context context, long startPosition, long endPosition, TSInfo info) {
        super(id, saveDir, targetFileName, createTime, status, errorMessage, url, resourceInfo, currentLength, context, startPosition, endPosition);
        this.info = info;
    }

    public TSInfo getInfo() {
        return info;
    }
}

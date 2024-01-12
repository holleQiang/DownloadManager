package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;

public interface TSDownloadBundleFactory {
    TSDownloadBundle createTSDownloadBundle(String saveDir, String targetFileName, String url, long startPosition, long endPosition,TSInfo info);
}

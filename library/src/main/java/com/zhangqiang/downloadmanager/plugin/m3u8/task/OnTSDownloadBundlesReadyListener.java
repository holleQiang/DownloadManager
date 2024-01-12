package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import java.util.List;

public interface OnTSDownloadBundlesReadyListener {

    void onTSBundlesReady(List<TSDownloadBundle> bundles);
}

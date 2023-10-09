package com.zhangqiang.downloadmanager.plugin.ftp.task;

import com.zhangqiang.downloadmanager.plugin.ftp.callback.ResourceInfo;

public interface OnResourceInfoReadyListener {

    void onResourceInfoReady(ResourceInfo resourceInfo);
}

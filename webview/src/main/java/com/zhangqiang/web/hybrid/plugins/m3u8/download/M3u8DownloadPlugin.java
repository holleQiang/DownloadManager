package com.zhangqiang.web.hybrid.plugins.m3u8.download;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;

import java.util.Date;
import java.util.UUID;

public class M3u8DownloadPlugin extends SimpleDownloadPlugin {

    @Override
    public String getName() {
        return "m3u8视频提取插件";
    }

    @Override
    protected void onApply(DownloadManager downloadManager) {
        super.onApply(downloadManager);
        downloadManager.addDownloadTaskFactory(new DownloadTaskFactory() {
            @Override
            public DownloadTask createTask(DownloadRequest request) {
                if(request instanceof M3u8DownloadRequest){
                    M3u8DownloadRequest m3u8DownloadRequest = (M3u8DownloadRequest) request;
                    return new M3u8DownloadTask(m3u8DownloadRequest.getUrl(), UUID.randomUUID().toString(),request.getSaveDir(),request.getTargetFileName(),new Date().getTime());
                }
                return null;
            }
        });
    }
}

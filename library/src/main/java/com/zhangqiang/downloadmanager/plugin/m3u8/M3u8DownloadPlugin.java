package com.zhangqiang.downloadmanager.plugin.m3u8;

import android.content.Context;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTaskFactory;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.request.M3u8DownloadRequest;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.M3u8DownloadTask;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.TSDownloadTask;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.TSDownloadTaskFactory;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;

import java.util.Date;
import java.util.UUID;

public class M3u8DownloadPlugin extends SimpleDownloadPlugin {

    private final Context context;

    public M3u8DownloadPlugin(Context context) {
        this.context = context.getApplicationContext();
    }

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
                if (request instanceof M3u8DownloadRequest) {
                    M3u8DownloadRequest m3u8DownloadRequest = (M3u8DownloadRequest) request;
                    return new M3u8DownloadTask(UUID.randomUUID().toString(), request.getSaveDir(), request.getTargetFileName(), new Date().getTime(), context, m3u8DownloadRequest.getUrl(), new TSDownloadTaskFactory() {
                        @Override
                        public TSDownloadTask createTSDownloadTask(String saveDir, String targetFileName, String url, long startPosition, long endPosition, TSInfo info) {
                            return new TSDownloadTask(
                                    UUID.randomUUID().toString(),
                                    saveDir,
                                    targetFileName,
                                    System.currentTimeMillis(),
                                    url,
                                    context,
                                    startPosition,
                                    endPosition,
                                    info);
                        }
                    });
                }
                return null;
            }
        });
    }
}

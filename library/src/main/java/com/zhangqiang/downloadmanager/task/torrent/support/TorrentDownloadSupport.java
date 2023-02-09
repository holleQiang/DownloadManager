package com.zhangqiang.downloadmanager.task.torrent.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.support.DownloadBundle;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.support.LocalTask;

import java.util.List;

public class TorrentDownloadSupport  implements DownloadSupport {
    @Override
    public List<LocalTask> loadLocalTasks() {
        return null;
    }

    @Override
    public DownloadBundle createDownloadBundle(String id, DownloadRequest request) {
        return null;
    }

    @Override
    public void handleProgressSync(DownloadBundle downloadBundle) {

    }

    @Override
    public void handleSpeedCompute(DownloadBundle downloadBundle) {

    }

    @Override
    public boolean isTaskIdle(DownloadBundle downloadBundle) {
        return false;
    }

    @Override
    public void handleDeleteTask(DownloadBundle downloadBundle, boolean deleteFile) {

    }
}

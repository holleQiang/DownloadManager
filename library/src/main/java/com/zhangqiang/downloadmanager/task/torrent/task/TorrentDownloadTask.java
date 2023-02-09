package com.zhangqiang.downloadmanager.task.torrent.task;

import com.zhangqiang.downloadmanager.task.DownloadTask;

public class TorrentDownloadTask extends DownloadTask {

    @Override
    protected void onStart() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    public long getCurrentLength() {
        return 0;
    }
}

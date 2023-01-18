package com.zhangqiang.downloadmanager.task.ftp;

import com.zhangqiang.downloadmanager.task.DownloadTask;

public class FTPDownloadTask extends DownloadTask {

    public FTPDownloadTask(String id) {
        super(id);
    }

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

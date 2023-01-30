package com.zhangqiang.downloadmanager.task.ftp;

import com.zhangqiang.downloadmanager.task.DownloadTask;

import org.apache.commons.net.ftp.FTPClient;

public class FTPDownloadTask extends DownloadTask {

    public FTPDownloadTask(String id) {
        super(id);
    }

    @Override
    protected void onStart() {
        FTPClient ftpClient = new FTPClient();
    }

    @Override
    protected void onCancel() {

    }

    @Override
    public long getCurrentLength() {
        return 0;
    }

}

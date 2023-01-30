package com.zhangqiang.downloadmanager.task.ftp;

import com.zhangqiang.downloadmanager.task.DownloadTask;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;

public class FTPDownloadTask extends DownloadTask {

    private String host;
    private int port;
    private String userName;
    private String password;

    public FTPDownloadTask(String id) {
        super(id);
    }

    @Override
    protected void onStart() {
        FTPClient ftpClient = new FTPClient();
        if (!ftpClient.isConnected()) {
            ftpClient.setConnectTimeout(5000);
            ftpClient.setDataTimeout(10000);
            ftpClient.enterLocalPassiveMode();
            try {
                ftpClient.connect(host,port);
                ftpClient.login(userName,password);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    public long getCurrentLength() {
        return 0;
    }

}

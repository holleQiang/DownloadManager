package com.zhangqiang.downloadmanager.plugin.ftp.request;

import com.zhangqiang.downloadmanager.request.DownloadRequest;

public class FtpDownloadRequest extends DownloadRequest {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String ftpDir;
    private final String ftpFileName;

    public FtpDownloadRequest(String saveDir, String targetFileName, String host, int port, String username, String password, String ftpDir, String ftpFileName) {
        super(saveDir, targetFileName);
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFtpDir() {
        return ftpDir;
    }

    public String getFtpFileName() {
        return ftpFileName;
    }
}

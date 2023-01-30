package com.zhangqiang.downloadmanager.task.ftp.request;

import com.zhangqiang.downloadmanager.DownloadRequest;

public class FTPDownloadRequest extends DownloadRequest {

    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final String ftpDir;
    private final String ftpFileName;

    FTPDownloadRequest(String saveDir, String fileName, String host, int port, String userName, String password, String ftpDir, String ftpFileName) {
        super(saveDir, fileName);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
    }

    public static class Builder {
        private String host;
        private int port;
        private String userName;
        private String password;
        private String saveDir;
        private String fileName;
        private String ftpDir;
        private String ftpFileName;

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setUserName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setSaveDir(String saveDir) {
            this.saveDir = saveDir;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setFtpDir(String ftpDir) {
            this.ftpDir = ftpDir;
            return this;
        }

        public Builder setFtpFileName(String ftpFileName) {
            this.ftpFileName = ftpFileName;
            return this;
        }

        public FTPDownloadRequest build() {
            return new FTPDownloadRequest(saveDir, fileName, host, port, userName, password, ftpDir, ftpFileName);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
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

package com.zhangqiang.downloadmanager.task.ftp.bean;

public class FTPTaskBean {
    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_CANCEL = 4;

    private String id;
    private String host;
    private int port;
    private String ftpDir;
    private String ftpFileName;
    private String saveDir;
    private String fileName;
    private String targetFileName;

    private int state = STATE_IDLE;
    private String errorMsg;

    public int getState() {
        return state;
    }

    public FTPTaskBean setState(int state) {
        this.state = state;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public FTPTaskBean setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public String getHost() {
        return host;
    }

    public FTPTaskBean setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public FTPTaskBean setPort(int port) {
        this.port = port;
        return this;
    }

    public String getFtpDir() {
        return ftpDir;
    }

    public FTPTaskBean setFtpDir(String ftpDir) {
        this.ftpDir = ftpDir;
        return this;
    }

    public String getFtpFileName() {
        return ftpFileName;
    }

    public FTPTaskBean setFtpFileName(String ftpFileName) {
        this.ftpFileName = ftpFileName;
        return this;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public FTPTaskBean setSaveDir(String saveDir) {
        this.saveDir = saveDir;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public FTPTaskBean setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public FTPTaskBean setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }
}

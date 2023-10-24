package com.zhangqiang.downloadmanager.plugin.ftp.bean;

public class FTPTaskBean {
    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_CANCEL = 4;

    private String id;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String ftpDir;
    private String ftpFileName;
    private String saveDir;
    private String targetFileName;
    private String saveFileName;

    private long currentLength;
    private long contentLength;
    private String contentType;
    private int state = STATE_IDLE;
    private String errorMsg;
    private long createTime;

    public String getId() {
        return id;
    }

    public FTPTaskBean setId(String id) {
        this.id = id;
        return this;
    }

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

    public String getTargetFileName() {
        return targetFileName;
    }

    public FTPTaskBean setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public FTPTaskBean setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
        return this;
    }

    public long getCreateTime() {
        return createTime;
    }

    public FTPTaskBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public FTPTaskBean setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public FTPTaskBean setPassword(String password) {
        this.password = password;
        return this;
    }

    public long getContentLength() {
        return contentLength;
    }

    public FTPTaskBean setContentLength(long contentLength) {
        this.contentLength = contentLength;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FTPTaskBean setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public FTPTaskBean setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
        return this;
    }

}

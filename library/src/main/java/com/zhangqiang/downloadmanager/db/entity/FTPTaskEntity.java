package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FTPTaskEntity {

    @Id
    private String id;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String ftpDir;
    private String ftpFileName;
    private String saveDir;
    private String fileName;
    private String targetFileName;

    private long currentLength;
    private long contentLength;
    private int state;
    private String errorMsg;
    private Date createTime;
    @Generated(hash = 1011100681)
    public FTPTaskEntity(String id, String host, int port, String userName,
            String password, String ftpDir, String ftpFileName, String saveDir,
            String fileName, String targetFileName, long currentLength,
            long contentLength, int state, String errorMsg, Date createTime) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
        this.saveDir = saveDir;
        this.fileName = fileName;
        this.targetFileName = targetFileName;
        this.currentLength = currentLength;
        this.contentLength = contentLength;
        this.state = state;
        this.errorMsg = errorMsg;
        this.createTime = createTime;
    }
    @Generated(hash = 763297361)
    public FTPTaskEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getHost() {
        return this.host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getFtpDir() {
        return this.ftpDir;
    }
    public void setFtpDir(String ftpDir) {
        this.ftpDir = ftpDir;
    }
    public String getFtpFileName() {
        return this.ftpFileName;
    }
    public void setFtpFileName(String ftpFileName) {
        this.ftpFileName = ftpFileName;
    }
    public String getSaveDir() {
        return this.saveDir;
    }
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getTargetFileName() {
        return this.targetFileName;
    }
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
    public long getCurrentLength() {
        return this.currentLength;
    }
    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public Date getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public long getContentLength() {
        return this.contentLength;
    }
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
}

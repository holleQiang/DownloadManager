package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

@Entity
public class HttpTaskEntity {

    @Id
    private String id;
    /**
     * 下载链接
     */
    @NotNull
    private String url;
    /**
     * 保存目录
     */
    @NotNull
    private String saveDir;
    /**
     * 文件名称，如果指定了会使用固定的名称
     */
    @NotNull
    private String fileName;
    /**
     * 文件长度
     */
    private long contentLength;
    /**
     * 文件mime type
     */
    private String contentType;
    @NotNull
    private Date createTime;
    private int threadSize;
    /**
     * 状态
     */
    @NotNull
    private int state;
    private String errorMsg;
    /**
     * 类型，普通下载/多线程下载
     */
    private int type;
    /**
     * 子任务id
     */
    private String childId;
    @Generated(hash = 1886720444)
    public HttpTaskEntity(String id, @NotNull String url, @NotNull String saveDir,
            @NotNull String fileName, long contentLength, String contentType,
            @NotNull Date createTime, int threadSize, int state, String errorMsg,
            int type, String childId) {
        this.id = id;
        this.url = url;
        this.saveDir = saveDir;
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.contentType = contentType;
        this.createTime = createTime;
        this.threadSize = threadSize;
        this.state = state;
        this.errorMsg = errorMsg;
        this.type = type;
        this.childId = childId;
    }
    @Generated(hash = 1367485091)
    public HttpTaskEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
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
    public long getContentLength() {
        return this.contentLength;
    }
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
    public String getContentType() {
        return this.contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public Date getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    public String getChildId() {
        return this.childId;
    }
    public void setChildId(String childId) {
        this.childId = childId;
    }
    public int getThreadSize() {
        return this.threadSize;
    }
    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}

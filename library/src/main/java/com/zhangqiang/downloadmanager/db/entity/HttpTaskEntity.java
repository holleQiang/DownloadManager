package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

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
     * 服务器返回的文件名称
     */
    private String fileName;
    /**
     * 服务器相应的码
     */
    private int responseCode;
    /**
     * 指定的文件名称
     */
    private String targetFileName;
    /**
     * 实际保存的文件名
     */
    private String saveFileName;
    /**
     * 文件长度
     */
    private long contentLength;
    /**
     * 文件mime type
     */
    private String contentType;
    @NotNull
    private long createTime;
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




    @Generated(hash = 160461520)
    public HttpTaskEntity(String id, @NotNull String url, @NotNull String saveDir,
            String fileName, int responseCode, String targetFileName,
            String saveFileName, long contentLength, String contentType,
            long createTime, int threadSize, int state, String errorMsg, int type,
            String childId) {
        this.id = id;
        this.url = url;
        this.saveDir = saveDir;
        this.fileName = fileName;
        this.responseCode = responseCode;
        this.targetFileName = targetFileName;
        this.saveFileName = saveFileName;
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

    public String getTargetFileName() {
        return this.targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
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

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getThreadSize() {
        return this.threadSize;
    }

    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
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

    public int getResponseCode() {
        return this.responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getSaveFileName() {
        return this.saveFileName;
    }

    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }




}

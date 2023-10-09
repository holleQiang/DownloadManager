package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class HttpPartTaskItemEntity {

    @Id
    private String id;
    @NotNull
    private String saveDir;
    /**
     * 实际保存的文件名
     */
    private String saveFileName;
    @NotNull
    private long startPosition;
    @NotNull
    private long currentLength;
    @NotNull
    private long endPosition;
    @NotNull
    private int state;
    @NotNull
    private long createTime;
    private String errorMsg;
    @Generated(hash = 1280415093)
    public HttpPartTaskItemEntity(String id, @NotNull String saveDir,
            String saveFileName, long startPosition, long currentLength,
            long endPosition, int state, long createTime, String errorMsg) {
        this.id = id;
        this.saveDir = saveDir;
        this.saveFileName = saveFileName;
        this.startPosition = startPosition;
        this.currentLength = currentLength;
        this.endPosition = endPosition;
        this.state = state;
        this.createTime = createTime;
        this.errorMsg = errorMsg;
    }
    @Generated(hash = 1397547861)
    public HttpPartTaskItemEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getSaveDir() {
        return this.saveDir;
    }
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }
    public String getSaveFileName() {
        return this.saveFileName;
    }
    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }
    public long getStartPosition() {
        return this.startPosition;
    }
    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }
    public long getCurrentLength() {
        return this.currentLength;
    }
    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }
    public long getEndPosition() {
        return this.endPosition;
    }
    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

}

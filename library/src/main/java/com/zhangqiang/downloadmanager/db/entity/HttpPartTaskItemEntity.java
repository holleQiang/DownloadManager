package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

@Entity
public class HttpPartTaskItemEntity {

    @Id
    private String id;
    @NotNull
    private String filePath;
    @NotNull
    private long startPosition;
    @NotNull
    private long currentPosition;
    @NotNull
    private long endPosition;
    @NotNull
    private int state;
    @NotNull
    private Date createTime;
    private String errorMsg;
    @Generated(hash = 1056940217)
    public HttpPartTaskItemEntity(String id, @NotNull String filePath,
            long startPosition, long currentPosition, long endPosition, int state,
            @NotNull Date createTime, String errorMsg) {
        this.id = id;
        this.filePath = filePath;
        this.startPosition = startPosition;
        this.currentPosition = currentPosition;
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
    public String getFilePath() {
        return this.filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public long getStartPosition() {
        return this.startPosition;
    }
    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }
    public long getCurrentPosition() {
        return this.currentPosition;
    }
    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
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
    public Date getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}

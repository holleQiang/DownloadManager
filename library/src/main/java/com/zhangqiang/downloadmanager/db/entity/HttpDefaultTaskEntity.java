package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

@Entity
public class HttpDefaultTaskEntity {

    @Id
    private String id;
    /**
     * 下载的进度
     */
    @NotNull
    private long currentLength;
    /**
     * 状态
     */
    @NotNull
    private int state;
    private String errorMsg;
    @NotNull
    private Date createTime;
    @Generated(hash = 1391573043)
    public HttpDefaultTaskEntity(String id, long currentLength, int state,
            String errorMsg, @NotNull Date createTime) {
        this.id = id;
        this.currentLength = currentLength;
        this.state = state;
        this.errorMsg = errorMsg;
        this.createTime = createTime;
    }
    @Generated(hash = 835410340)
    public HttpDefaultTaskEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
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
}

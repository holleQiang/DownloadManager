package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class HttpPartTaskEntity {

    @Id
    private String id;
    /**
     * 状态
     */
    @NotNull
    private int state;
    @NotNull
    private long createTime;
    private String errorMsg;
    /**
     * 子任务id
     */
    private String itemIds;

    @Generated(hash = 691279142)
    public HttpPartTaskEntity(String id, int state, long createTime,
                              String errorMsg, String itemIds) {
        this.id = id;
        this.state = state;
        this.createTime = createTime;
        this.errorMsg = errorMsg;
        this.itemIds = itemIds;
    }

    @Generated(hash = 1198508935)
    public HttpPartTaskEntity() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getItemIds() {
        return this.itemIds;
    }

    public void setItemIds(String itemIds) {
        this.itemIds = itemIds;
    }

}

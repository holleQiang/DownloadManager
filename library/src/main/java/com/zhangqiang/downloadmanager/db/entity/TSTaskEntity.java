package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class TSTaskEntity {
    @Id
    private String id;
    @NotNull
    private String url;
    private long duration;
    private String childId;
    @Generated(hash = 70770165)
    public TSTaskEntity(String id, @NotNull String url, long duration,
            String childId) {
        this.id = id;
        this.url = url;
        this.duration = duration;
        this.childId = childId;
    }
    @Generated(hash = 488424899)
    public TSTaskEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public long getDuration() {
        return this.duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public String getChildId() {
        return this.childId;
    }
    public void setChildId(String childId) {
        this.childId = childId;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
}

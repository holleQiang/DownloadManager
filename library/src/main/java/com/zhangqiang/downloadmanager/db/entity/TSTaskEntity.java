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
    private String uri;
    private float duration;
    private String childId;
    @Generated(hash = 1544692718)
    public TSTaskEntity(String id, @NotNull String uri, float duration,
            String childId) {
        this.id = id;
        this.uri = uri;
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
    public String getUri() {
        return this.uri;
    }
    public void setUri(String uri) {
        this.uri = uri;
    }
    public float getDuration() {
        return this.duration;
    }
    public void setDuration(float duration) {
        this.duration = duration;
    }
    public String getChildId() {
        return this.childId;
    }
    public void setChildId(String childId) {
        this.childId = childId;
    }

}

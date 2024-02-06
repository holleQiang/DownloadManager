package com.zhangqiang.web.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class VisitRecordEntity {

    @Id
    String id;
    private byte[] icon;
    private String title;
    @NotNull
    private String url;
    @Generated(hash = 384446102)
    public VisitRecordEntity(String id, byte[] icon, String title,
            @NotNull String url) {
        this.id = id;
        this.icon = icon;
        this.title = title;
        this.url = url;
    }
    @Generated(hash = 933744019)
    public VisitRecordEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public byte[] getIcon() {
        return this.icon;
    }
    public void setIcon(byte[] icon) {
        this.icon = icon;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getTitle() {
        return this.title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}

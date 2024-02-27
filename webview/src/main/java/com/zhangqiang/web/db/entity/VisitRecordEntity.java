package com.zhangqiang.web.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class VisitRecordEntity {

    @Id
    String id;
    private String iconUrl;
    private String title;
    @NotNull
    private String url;
    private long visitDate;

    @Generated(hash = 1469410847)
    public VisitRecordEntity(String id, String iconUrl, String title,
            @NotNull String url, long visitDate) {
        this.id = id;
        this.iconUrl = iconUrl;
        this.title = title;
        this.url = url;
        this.visitDate = visitDate;
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
    public long getVisitDate() {
        return this.visitDate;
    }
    public void setVisitDate(long visitDate) {
        this.visitDate = visitDate;
    }
    public String getIconUrl() {
        return this.iconUrl;
    }
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}

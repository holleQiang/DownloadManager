package com.zhangqiang.web.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class BookMarkEntity {

    @Id
    private String id;

    private String title;

    private String url;

    private String iconUrl;

    private String childIds;

    private String parentId;

    @Generated(hash = 1258364236)
    public BookMarkEntity(String id, String title, String url, String iconUrl,
            String childIds, String parentId) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.iconUrl = iconUrl;
        this.childIds = childIds;
        this.parentId = parentId;
    }

    @Generated(hash = 1463584955)
    public BookMarkEntity() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParentId() {
        return this.parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getChildIds() {
        return this.childIds;
    }

    public void setChildIds(String childIds) {
        this.childIds = childIds;
    }

    public String getIconUrl() {
        return this.iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }


}

package com.zhangqiang.web.history.bean;

import android.graphics.Bitmap;

public class VisitRecordBean {
    private String id;
    private Bitmap icon;
    private String url;
    private String title;

    public Bitmap getIcon() {
        return icon;
    }

    public VisitRecordBean setIcon(Bitmap icon) {
        this.icon = icon;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public VisitRecordBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public VisitRecordBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getId() {
        return id;
    }

    public VisitRecordBean setId(String id) {
        this.id = id;
        return this;
    }
}

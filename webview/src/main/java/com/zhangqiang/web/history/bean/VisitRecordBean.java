package com.zhangqiang.web.history.bean;


public class VisitRecordBean {
    private String id;
    private String iconUrl;
    private String url;
    private String title;
    private long visitDate;

    public String getIconUrl() {
        return iconUrl;
    }

    public VisitRecordBean setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
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

    public long getVisitDate() {
        return visitDate;
    }

    public VisitRecordBean setVisitDate(long visitDate) {
        this.visitDate = visitDate;
        return this;
    }
}

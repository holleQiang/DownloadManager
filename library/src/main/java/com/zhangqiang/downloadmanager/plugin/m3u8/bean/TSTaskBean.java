package com.zhangqiang.downloadmanager.plugin.m3u8.bean;

import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskItemBean;

public class TSTaskBean {

    private String id;
    private String uri;
    private float duration;
    private HttpPartTaskItemBean httpPartTaskItemBean;

    public String getId() {
        return id;
    }

    public TSTaskBean setId(String id) {
        this.id = id;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public TSTaskBean setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public HttpPartTaskItemBean getHttpPartTaskItemBean() {
        return httpPartTaskItemBean;
    }

    public TSTaskBean setHttpPartTaskItemBean(HttpPartTaskItemBean httpPartTaskItemBean) {
        this.httpPartTaskItemBean = httpPartTaskItemBean;
        return this;
    }

    public String getUri() {
        return uri;
    }

    public TSTaskBean setUri(String uri) {
        this.uri = uri;
        return this;
    }
}

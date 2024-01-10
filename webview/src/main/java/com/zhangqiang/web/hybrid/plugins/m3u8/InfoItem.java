package com.zhangqiang.web.hybrid.plugins.m3u8;

public class InfoItem {

    private  float duration;
    private  String uri;

    public InfoItem setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public InfoItem setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }
}

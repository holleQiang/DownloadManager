package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

public class TSInfo {

    private  float duration;
    private  String uri;

    public TSInfo setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public TSInfo setUri(String uri) {
        this.uri = uri;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "InfoItem{" +
                "duration=" + duration +
                ", uri='" + uri + '\'' +
                '}';
    }
}

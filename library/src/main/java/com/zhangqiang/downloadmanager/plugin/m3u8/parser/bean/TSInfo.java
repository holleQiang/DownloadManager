package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

public class TSInfo {

    private final float duration;
    private final String uri;

    public TSInfo(float duration, String uri) {
        this.duration = duration;
        this.uri = uri;
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

package com.zhangqiang.downloadmanager.plugin.m3u8.task;

public class M3u8ResourceInfo {

    private final long duration;

    public M3u8ResourceInfo(long totalDuration) {
        this.duration = totalDuration;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                "totalDuration=" + duration +
                '}';
    }
}

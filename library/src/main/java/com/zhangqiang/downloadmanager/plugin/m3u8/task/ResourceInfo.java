package com.zhangqiang.downloadmanager.plugin.m3u8.task;

public class ResourceInfo {

    private final long totalDuration;

    public ResourceInfo(long totalDuration) {
        this.totalDuration = totalDuration;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    @Override
    public String toString() {
        return "ResourceInfo{" +
                "totalDuration=" + totalDuration +
                '}';
    }
}

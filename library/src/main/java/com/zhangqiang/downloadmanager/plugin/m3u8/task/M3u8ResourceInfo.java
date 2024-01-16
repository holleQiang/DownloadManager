package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;

public class M3u8ResourceInfo {

    private final float duration;
    private final M3u8File m3u8File;

    public M3u8ResourceInfo(float totalDuration, M3u8File m3u8File) {
        this.duration = totalDuration;
        this.m3u8File = m3u8File;
    }

    public float getDuration() {
        return duration;
    }

    public M3u8File getM3u8File() {
        return m3u8File;
    }

    @Override
    public String toString() {
        return "M3u8ResourceInfo{" +
                "duration=" + duration +
                ", m3u8File=" + m3u8File +
                '}';
    }
}

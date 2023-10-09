package com.zhangqiang.downloadmanager.plugin.http.range;

public class RangeItem {
    private long start;
    private long end;
    private long current;
    private String savePath;

    public RangeItem(long start, long end, long current, String savePath) {
        this.start = start;
        this.end = end;
        this.current = current;
        this.savePath = savePath;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getCurrent() {
        return current;
    }

    public String getSavePath() {
        return savePath;
    }
}

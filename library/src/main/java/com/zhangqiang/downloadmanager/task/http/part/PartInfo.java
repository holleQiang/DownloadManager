package com.zhangqiang.downloadmanager.task.http.part;

public class PartInfo {

    private final long start;
    private final long end;

    public PartInfo(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}

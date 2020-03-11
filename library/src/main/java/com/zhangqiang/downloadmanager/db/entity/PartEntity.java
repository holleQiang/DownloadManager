package com.zhangqiang.downloadmanager.db.entity;

import com.zhangqiang.db.entity.DBEntity;

public class PartEntity implements DBEntity {

    private String url;
    private String savePath;
    private long current;
    private long start;
    private long end;
    private int status;

    @Override
    public String getUniqueId() {
        return savePath;
    }

    public String getUrl() {
        return url;
    }

    public PartEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getSavePath() {
        return savePath;
    }

    public PartEntity setSavePath(String savePath) {
        this.savePath = savePath;
        return this;
    }

    public long getCurrent() {
        return current;
    }

    public PartEntity setCurrent(long current) {
        this.current = current;
        return this;
    }

    public long getStart() {
        return start;
    }

    public PartEntity setStart(long start) {
        this.start = start;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public PartEntity setEnd(long end) {
        this.end = end;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public PartEntity setStatus(int status) {
        this.status = status;
        return this;
    }
}

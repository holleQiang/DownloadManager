package com.zhangqiang.downloadmanager.db.entity;

import android.support.annotation.NonNull;

import com.zhangqiang.db.entity.DBEntity;

public class TaskEntity implements DBEntity {

    private String url;
    private String saveDir;
    private String fileName;
    private long currentLength;
    private long totalLength;
    private int state;
    private String eTag;
    private String lastModified;
    private String contentType;

    @NonNull
    @Override
    public String getUniqueId() {
        return url;
    }

    public String getUrl() {
        return url;
    }

    public TaskEntity setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public TaskEntity setSaveDir(String saveDir) {
        this.saveDir = saveDir;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public TaskEntity setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public TaskEntity setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
        return this;
    }

    public long getTotalLength() {
        return totalLength;
    }

    public TaskEntity setTotalLength(long totalLength) {
        this.totalLength = totalLength;
        return this;
    }

    public int getState() {
        return state;
    }

    public TaskEntity setState(int state) {
        this.state = state;
        return this;
    }

    public String getETag() {
        return eTag;
    }

    public TaskEntity setETag(String eTag) {
        this.eTag = eTag;
        return this;
    }

    public String getLastModified() {
        return lastModified;
    }

    public TaskEntity setLastModified(String lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public TaskEntity setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

}

package com.zhangqiang.downloadmanager;

public interface TaskInfo {

    Long getId();

    String getUrl();

    String getSaveDir();

    String getFileName();

    long getCurrentLength();

    long getContentLength();

    int getState();

    String getETag();

    String getLastModified();

    String getContentType();

    long getCreateTime();

    String getErrorMsg();

    int getThreadSize();

    long getSpeed();

    long getThreadSpeed(int threadIndex);

    long getThreadCurrentLength(int threadIndex);
}

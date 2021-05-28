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

    int getPartSize();

    long getPartSpeed(int partIndex);

    long getPartLength(int partIndex);

    long getTotalPartLength(int partIndex);
}

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

    int getThreadCount();

    long getSpeed();

    int getPartCount();

    long getPartSpeed(int partIndex);

    long getPartCurrentLength(int partIndex);

    long getPartContentLength(int partIndex);
}

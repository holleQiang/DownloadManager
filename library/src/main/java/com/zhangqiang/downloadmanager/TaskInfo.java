package com.zhangqiang.downloadmanager;

public interface TaskInfo {

    String getId();

    long getCurrentLength();

    long getContentLength();

    String getSaveDir();

    String getFileName();

    long getCreateTime();

    String getContentType();
}

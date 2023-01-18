package com.zhangqiang.downloadmanager;

public interface TaskInfo {

    int STATE_IDLE = 0;
    int STATE_FAIL = 1;
    int STATE_PAUSE = 2;
    int STATE_DOWNLOADING = 3;
    int STATE_COMPLETE = 4;

    String getId();

    String getUrl();

    String getSaveDir();

    String getFileName();

    long getCurrentLength();

    long getContentLength();

    int getState();

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

package com.zhangqiang.downloadmanager.task.ftp.support;

import com.zhangqiang.downloadmanager.TaskInfo;

public interface FTPTaskInfo extends TaskInfo {

    int STATE_IDLE = 0;
    int STATE_FAIL = 1;
    int STATE_PAUSE = 2;
    int STATE_DOWNLOADING = 3;
    int STATE_COMPLETE = 4;

    int getState();

    String getErrorMsg();

    long getSpeed();

    String getHost();

    int getPort();

    String getUserName();

    String getPassword();

    String getFtpDir();

    String getFtpFileName();

    interface Listener{

        void onStateChanged();

        void onInfoReady();

        void onProgressChanged();

        void onSpeedChanged();
    }

    void addListener(Listener listener);

    void removeListener(Listener listener);
}

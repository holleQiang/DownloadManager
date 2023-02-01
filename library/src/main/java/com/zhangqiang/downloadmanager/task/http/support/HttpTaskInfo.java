package com.zhangqiang.downloadmanager.task.http.support;

import com.zhangqiang.downloadmanager.TaskInfo;

public interface HttpTaskInfo extends TaskInfo {

    int STATE_IDLE = 0;
    int STATE_FAIL = 1;
    int STATE_PAUSE = 2;
    int STATE_DOWNLOADING = 3;
    int STATE_COMPLETE = 4;

    String getUrl();

    int getState();

    String getErrorMsg();

    int getThreadCount();

    long getSpeed();

    int getPartCount();

    long getPartSpeed(int partIndex);

    long getPartCurrentLength(int partIndex);

    long getPartContentLength(int partIndex);

    interface Listener{

        void onStateChanged();

        void onInfoReady();

        void onProgressChanged();

        void onSpeedChanged();
    }

    void addListener(Listener listener);

    void removeListener(Listener listener);
}

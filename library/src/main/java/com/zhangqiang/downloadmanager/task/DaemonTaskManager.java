package com.zhangqiang.downloadmanager.task;

import android.os.Handler;
import android.os.HandlerThread;

public class DaemonTaskManager {

    private static volatile DaemonTaskManager instance;
    private final Handler mHandler;

    public static DaemonTaskManager getInstance() {
        if (instance == null) {
            synchronized (DaemonTaskManager.class) {
                if (instance == null) {
                    instance = new DaemonTaskManager();
                }
            }
        }
        return instance;
    }

    private DaemonTaskManager() {
        HandlerThread handlerThread = new HandlerThread("download_daemon");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());
    }

    public void post(Runnable runnable){

    }

    public void sendMsgIfNotExists(int what) {
        if (mHandler.hasMessages(what)) {
            return;
        }
    }
}

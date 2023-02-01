package com.zhangqiang.downloadmanager.listeners;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class UIDownloadTaskListener implements DownloadTaskListener {

    public static final int MSG_TASK_ADDED = 0;
    public static final int MSG_TASK_REMOVED = 1;
    public static final int MSG_ACTIVE_TASK_SIZE_CHANGED = 6;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TASK_ADDED:
                    onTaskAddedMain(((String) msg.obj));
                    break;
                case MSG_TASK_REMOVED:
                    onTaskRemovedMain(((String) msg.obj));
                    break;
                case MSG_ACTIVE_TASK_SIZE_CHANGED:
                    onActiveTaskSizeChangedMain();
                    break;
            }
        }
    };

    public abstract void onTaskRemovedMain(String id);

    public abstract void onTaskAddedMain(String id);

    public abstract void onActiveTaskSizeChangedMain();

    @Override
    public void onTaskAdded(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_ADDED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskRemoved(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_REMOVED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onActiveTaskSizeChanged() {
        Message message = mHandler.obtainMessage(MSG_ACTIVE_TASK_SIZE_CHANGED);
        mHandler.sendMessage(message);
    }
}

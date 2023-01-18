package com.zhangqiang.downloadmanager.listeners;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class UIDownloadTaskListener implements DownloadTaskListener {

    public static final int MSG_TASK_ADDED = 0;
    public static final int MSG_TASK_REMOVED = 1;
    public static final int MSG_TASK_STATE_CHANGED = 2;
    public static final int MSG_TASK_INFO_CHANGED = 3;
    public static final int MSG_TASK_PROGRESS_CHANGED = 4;
    public static final int MSG_TASK_SPEED_CHANGED = 5;
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
                case MSG_TASK_STATE_CHANGED:
                    onTaskStateChangedMain(((String) msg.obj));
                    break;
                case MSG_TASK_INFO_CHANGED:
                    onTaskInfoChangedMain(((String) msg.obj));
                    break;
                case MSG_TASK_PROGRESS_CHANGED:
                    onTaskProgressChangedMain(((String) msg.obj));
                    break;
                case MSG_TASK_SPEED_CHANGED:
                    onTaskSpeedChangedMain(((String) msg.obj));
                    break;
                    case MSG_ACTIVE_TASK_SIZE_CHANGED:
                        onActiveTaskSizeChangedMain();
                    break;
            }
        }
    };

    public abstract void onTaskProgressChangedMain(String id);

    protected abstract void onTaskSpeedChangedMain(String id);

    public abstract void onTaskInfoChangedMain(String id);

    public abstract void onTaskStateChangedMain(String id);

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
    public void onTaskStateChanged(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_STATE_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskInfoChanged(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_INFO_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskProgressChanged(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_PROGRESS_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskSpeedChanged(String id) {
        Message message = mHandler.obtainMessage(MSG_TASK_SPEED_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onActiveTaskSizeChanged() {
        Message message = mHandler.obtainMessage(MSG_ACTIVE_TASK_SIZE_CHANGED);
        mHandler.sendMessage(message);
    }
}

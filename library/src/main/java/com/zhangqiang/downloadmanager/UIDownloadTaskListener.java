package com.zhangqiang.downloadmanager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public abstract class UIDownloadTaskListener implements DownloadTaskListener{

    public static final int MSG_TASK_ADDED = 0;
    public static final int MSG_TASK_REMOVED = 1;
    public static final int MSG_TASK_STATE_CHANGED = 2;
    public static final int MSG_TASK_INFO_CHANGED = 3;
    public static final int MSG_TASK_PROGRESS_CHANGED = 4;

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TASK_ADDED:
                    onTaskAddedMain(((long) msg.obj));
                    break;
                case MSG_TASK_REMOVED:
                    onTaskRemovedMain(((long) msg.obj));
                    break;
                case MSG_TASK_STATE_CHANGED:
                    onTaskStateChangedMain(((long) msg.obj));
                    break;
                case MSG_TASK_INFO_CHANGED:
                    onTaskInfoChangedMain(((long) msg.obj));
                    break;
                case MSG_TASK_PROGRESS_CHANGED:
                    onTaskProgressChangedMain(((long) msg.obj));
                    break;
            }
        }
    };

    public abstract void onTaskProgressChangedMain(long id);

    public abstract void onTaskInfoChangedMain(long id);

    public abstract void onTaskStateChangedMain(long id);

    public abstract void onTaskRemovedMain(long id);

    public abstract void onTaskAddedMain(long id);

    @Override
    public void onTaskAdded(long id) {
        Message message = mHandler.obtainMessage(MSG_TASK_ADDED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskRemoved(long id) {
        Message message = mHandler.obtainMessage(MSG_TASK_REMOVED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskStateChanged(long id) {
        Message message = mHandler.obtainMessage(MSG_TASK_STATE_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskInfoChanged(long id) {
        Message message = mHandler.obtainMessage(MSG_TASK_INFO_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }

    @Override
    public void onTaskProgressChanged(long id) {
        Message message = mHandler.obtainMessage(MSG_TASK_PROGRESS_CHANGED);
        message.obj = id;
        mHandler.sendMessage(message);
    }
}

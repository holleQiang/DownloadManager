package com.zhangqiang.downloadmanager.helper;

import com.zhangqiang.downloadmanager.task.DaemonTaskManager;

public abstract class ProgressUpdateHelper {

    private boolean updating;
    private boolean hasPendingUpdate;

    public void update() {
        if (updating) {
            hasPendingUpdate = true;
            return;
        }
        updating = true;
        DaemonTaskManager.getInstance().post(updateTask);
    }

    private Runnable updateTask = new Runnable() {
        @Override
        public void run() {
            doUpdate();
            updating = false;
            if (hasPendingUpdate) {
                DaemonTaskManager.getInstance().post(this);
                hasPendingUpdate = false;
            }
        }
    };

    protected abstract void doUpdate();
}

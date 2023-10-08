package com.zhangqiang.downloadmanager2.task;

public interface OnStatusChangeListener {

    void onStatusChange(Status newStatus,Status oldStatus);
}

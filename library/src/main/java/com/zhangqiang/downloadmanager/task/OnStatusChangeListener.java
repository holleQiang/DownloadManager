package com.zhangqiang.downloadmanager.task;

public interface OnStatusChangeListener {

    void onStatusChange(Status newStatus, Status oldStatus);
}

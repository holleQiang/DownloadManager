package com.zhangqiang.downloadmanager2.task;

import java.util.ArrayList;
import java.util.List;

public abstract class DownloadTask {

    private final String saveDir;
    private final String targetFileName;
    private final long createTime;
    private Status status = Status.IDLE;
    private String errorMessage;

    private final List<OnStatusChangeListener> onStatusChangeListeners = new ArrayList<>();
    private final List<OnTaskFailListener> onTaskFailListeners = new ArrayList<>();

    public DownloadTask(String saveDir, String targetFileName, long createTime) {
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
    }

    public DownloadTask(String saveDir, String targetFileName, long createTime, Status status, String errorMessage) {
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public void start() {
        if (status == Status.DOWNLOADING) {
            throw new IllegalStateException("cannot start from downloading status");
        }
        Status oldStatus = status;
        status = Status.DOWNLOADING;
        dispatchStatusChange(Status.DOWNLOADING, oldStatus);
        forceStart();
    }

    public void forceStart() {
        try {
            onStart();
        } catch (Throwable e) {
            dispatchFail(e);
        }
    }

    protected abstract void onStart();

    public void cancel() {
        if (status != Status.DOWNLOADING) {
            throw new IllegalStateException("cancel not from downloading status");
        }

        status = Status.CANCELED;
        onCancel();
        dispatchStatusChange(Status.CANCELED, Status.DOWNLOADING);
    }

    protected abstract void onCancel();

    public void addStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        onStatusChangeListeners.add(onStatusChangeListener);
    }

    public void removeStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        onStatusChangeListeners.remove(onStatusChangeListener);
    }

    public Status getStatus() {
        return status;
    }

    protected void dispatchSuccess() {
        if (status != Status.DOWNLOADING) {
            throw new IllegalStateException("dispatch success from no downloading status");
        }
        status = Status.SUCCESS;
        dispatchStatusChange(Status.SUCCESS, Status.DOWNLOADING);
    }

    protected void dispatchFail(Throwable e) {
        if (status != Status.DOWNLOADING && status != Status.CANCELED) {
            throw new IllegalStateException("dispatch fail from  status:" + status);
        }
        status = Status.FAIL;
        errorMessage = e.getMessage();
        dispatchStatusChange(Status.FAIL, Status.DOWNLOADING);
        for (int i = onTaskFailListeners.size() - 1; i >= 0; i--) {
            onTaskFailListeners.get(i).onTaskFail(e);
        }
    }

    private void dispatchStatusChange(Status newStatus, Status oldStatus) {
        for (int i = onStatusChangeListeners.size() - 1; i >= 0; i--) {
            onStatusChangeListeners.get(i).onStatusChange(newStatus, oldStatus);
        }
    }

    public long getCreateTime() {
        return createTime;
    }

    public abstract String getSaveFileName();

    public void addTaskFailListener(OnTaskFailListener listener) {
        onTaskFailListeners.add(listener);
    }

    public void removeTaskFailListener(OnTaskFailListener listener) {
        onTaskFailListeners.remove(listener);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public String getTargetFileName() {
        return targetFileName;
    }


}

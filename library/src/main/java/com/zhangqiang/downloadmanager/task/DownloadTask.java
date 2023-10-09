package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.schedule.IntervalTask;
import com.zhangqiang.downloadmanager.schedule.Schedule;
import com.zhangqiang.downloadmanager.speed.SpeedHelper;
import com.zhangqiang.downloadmanager.speed.SpeedSupport;
import com.zhangqiang.downloadmanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class DownloadTask implements SpeedSupport, CurrentLengthOwner{

    private final String saveDir;
    private final String targetFileName;
    private final long createTime;
    private Status status = Status.IDLE;
    private String errorMessage;
    private long currentLength;
    private long initialLength;
    private final List<OnStatusChangeListener> onStatusChangeListeners = new ArrayList<>();
    private final List<OnTaskFailListener> onTaskFailListeners = new ArrayList<>();
    private final List<OnProgressChangeListener> onProgressChangeListeners = new ArrayList<>();
    private final SpeedHelper speedHelper = new SpeedHelper(this);
    private final IntervalTask progressTask = new IntervalTask(300) {

        long lastLength = getCurrentLength();
        @Override
        public void run() {
            long currentLength = getCurrentLength();
            if(lastLength !=currentLength){
                dispatchProgressChange();
                lastLength = currentLength;
            }
        }
    };
    private final IntervalTask speedTask = new IntervalTask(3000) {
        @Override
        public void run() {
            getSpeedHelper().calculateSpeed();
        }
    };

    public DownloadTask(String saveDir, String targetFileName, long createTime) {
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
    }

    public DownloadTask(String saveDir, String targetFileName, long createTime, Status status, String errorMessage,long currentLength) {
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
        this.status = status;
        this.errorMessage = errorMessage;
        this.currentLength = this.initialLength = currentLength;
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

    public void addOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListeners.add(listener);
    }

    public void removeOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListeners.remove(listener);
    }

    protected void dispatchProgressChange() {
        for (int i = onProgressChangeListeners.size() - 1; i >= 0; i--) {
            onProgressChangeListeners.get(i).onProgressChange();
        }
    }

    protected void startScheduleProgress() {
        Schedule.getInstance().startSchedule(progressTask);
        Schedule.getInstance().startSchedule(speedTask);
    }

    protected void stopScheduleProgressChange() {
        Schedule.getInstance().stopSchedule(progressTask);
        Schedule.getInstance().stopSchedule(speedTask);
    }

    protected void dispatchCurrentLength(long currentLength){
        this.currentLength  = currentLength;
    }

    public long getInitialLength() {
        return initialLength;
    }

    @Override
    public SpeedHelper getSpeedHelper() {
        return speedHelper;
    }

    @Override
    public long getCurrentLength() {
        return currentLength;
    }

    private void dispatchFileSaveStart() {
        startScheduleProgress();
    }

    private void dispatchFileSaveEnd() {
        stopScheduleProgressChange();
    }

    private void dispatchFileSaveLength(long length) {
        dispatchCurrentLength(getCurrentLength()+length);
    }

    protected void performSaveFile(InputStream inputStream) throws IOException {
        try {
            File saveDir = new File(getSaveDir());
            if (!saveDir.exists() || !saveDir.isDirectory()) {
                if (!saveDir.mkdirs()) {
                    throw new IOException("create dir fail: " + saveDir.getAbsolutePath());
                }
            }
            File saveFile = new File(getSaveDir(), getSaveFileName());
            dispatchFileSaveStart();
            FileUtils.writeToFileFrom(inputStream, saveFile, getCurrentLength(), new FileUtils.WriteFileListener() {
                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    dispatchFileSaveLength(len);
                }
            });
        } finally {
            dispatchFileSaveEnd();
        }
    }
}

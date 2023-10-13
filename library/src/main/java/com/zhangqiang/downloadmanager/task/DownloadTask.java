package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.schedule.IntervalTask;
import com.zhangqiang.downloadmanager.schedule.Schedule;
import com.zhangqiang.downloadmanager.speed.SpeedHelper;
import com.zhangqiang.downloadmanager.speed.SpeedSupport;
import com.zhangqiang.downloadmanager.task.interceptor.fail.FailChain;
import com.zhangqiang.downloadmanager.task.interceptor.fail.FailInterceptor;
import com.zhangqiang.downloadmanager.task.interceptor.fail.RealFailChain;
import com.zhangqiang.downloadmanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class DownloadTask implements SpeedSupport, CurrentLengthOwner {

    private final String id;
    private final String saveDir;
    private final String targetFileName;
    private final long createTime;
    private AtomicReference<Status> status = new AtomicReference<>(Status.IDLE);
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
            if (lastLength != currentLength) {
                dispatchProgressChange();
                lastLength = currentLength;
            }
        }
    };
    private final List<FailInterceptor> failInterceptors = new ArrayList<>();


    public DownloadTask(String id, String saveDir, String targetFileName, long createTime) {
        this.id = id;
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
    }

    public DownloadTask(String id,
                        String saveDir,
                        String targetFileName,
                        long createTime,
                        Status status,
                        String errorMessage,
                        long currentLength) {
        this.id = id;
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.createTime = createTime;
        this.status = new AtomicReference<>(status);
        this.errorMessage = errorMessage;
        this.currentLength = this.initialLength = currentLength;
    }


    public void start() {
        if (status.get() == Status.DOWNLOADING) {
            throw new IllegalStateException("cannot start from downloading status");
        }
        Status oldStatus = status.getAndSet(Status.DOWNLOADING);
        dispatchStatusChange(Status.DOWNLOADING, oldStatus);
        forceStart();
    }

    public void forceStart() {
        if (getStatus() != Status.DOWNLOADING) {
            throw new IllegalStateException("cannot call forceStart when status are not downloading");
        }
        try {
            onStart();
        } catch (Throwable e) {
            dispatchFail(e);
        }
    }

    protected abstract void onStart();

    public void cancel() {

        if (!status.compareAndSet(Status.DOWNLOADING, Status.CANCELED)) {
            throw new IllegalStateException("cancel not from downloading status");
        }
        onCancel();
        dispatchStatusChange(Status.CANCELED, Status.DOWNLOADING);
    }

    protected abstract void onCancel();

    public void addStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        synchronized (onStatusChangeListeners) {
            onStatusChangeListeners.add(onStatusChangeListener);
        }
    }

    public synchronized void removeStatusChangeListener(OnStatusChangeListener onStatusChangeListener) {
        synchronized (onStatusChangeListeners) {
            onStatusChangeListeners.remove(onStatusChangeListener);
        }
    }

    public Status getStatus() {
        return status.get();
    }

    protected void dispatchSuccess() {
        if (!status.compareAndSet(Status.DOWNLOADING, Status.SUCCESS)) {
            throw new IllegalStateException("dispatch success from no downloading status");
        }
        dispatchStatusChange(Status.SUCCESS, Status.DOWNLOADING);
    }

    protected void dispatchFail(Throwable e) {
        synchronized (failInterceptors) {
            List<FailInterceptor> finalInterceptors = new ArrayList<>(failInterceptors);
            finalInterceptors.add(new DispatchFailInterceptor());
            FailChain chain = new RealFailChain(e, finalInterceptors, 0);
            chain.proceed(e);
        }
    }

    private class DispatchFailInterceptor implements FailInterceptor {

        @Override
        public void onIntercept(FailChain chain) {
            if (status.get() == Status.CANCELED) {
                return;
            }
            if (!status.compareAndSet(Status.DOWNLOADING, Status.FAIL)) {
                throw new IllegalStateException("dispatch fail from  status:" + status);
            }
            Throwable e = chain.getThrowable();
            errorMessage = e.getMessage();
            dispatchStatusChange(Status.FAIL, Status.DOWNLOADING);
            dispatchTaskFail(e);
        }
    }


    private void dispatchStatusChange(Status newStatus, Status oldStatus) {
        synchronized (onStatusChangeListeners) {
            for (int i = onStatusChangeListeners.size() - 1; i >= 0; i--) {
                onStatusChangeListeners.get(i).onStatusChange(newStatus, oldStatus);
            }
        }
    }

    public long getCreateTime() {
        return createTime;
    }

    public abstract String getSaveFileName();

    public void addTaskFailListener(OnTaskFailListener listener) {
        synchronized (onTaskFailListeners) {
            onTaskFailListeners.add(listener);
        }
    }

    public void removeTaskFailListener(OnTaskFailListener listener) {
        synchronized (onTaskFailListeners) {
            onTaskFailListeners.remove(listener);
        }
    }

    private void dispatchTaskFail(Throwable e) {
        synchronized (onTaskFailListeners) {
            for (int i = onTaskFailListeners.size() - 1; i >= 0; i--) {
                onTaskFailListeners.get(i).onTaskFail(e);
            }
        }
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
        synchronized (onProgressChangeListeners) {
            onProgressChangeListeners.add(listener);
        }
    }

    public void removeOnProgressChangeListener(OnProgressChangeListener listener) {
        synchronized (onProgressChangeListeners) {
            onProgressChangeListeners.remove(listener);
        }
    }

    protected void dispatchProgressChange() {
        synchronized (onProgressChangeListeners) {
            for (int i = onProgressChangeListeners.size() - 1; i >= 0; i--) {
                onProgressChangeListeners.get(i).onProgressChange();
            }
        }
    }

    protected void startScheduleProgress() {
        Schedule.getInstance().startSchedule(progressTask);
        getSpeedHelper().start();
    }

    protected void stopScheduleProgressChange() {
        Schedule.getInstance().stopSchedule(progressTask);
        getSpeedHelper().stop();
    }

    protected void dispatchCurrentLength(long currentLength) {
        this.currentLength = currentLength;
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
        dispatchCurrentLength(getCurrentLength() + length);
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

    public void addFailInterceptor(FailInterceptor interceptor) {
        synchronized (failInterceptors) {
            failInterceptors.add(interceptor);
        }
    }

    public void removeFailInterceptor(FailInterceptor interceptor) {
        synchronized (failInterceptors) {
            failInterceptors.remove(interceptor);
        }
    }

    public String getId() {
        return id;
    }
}

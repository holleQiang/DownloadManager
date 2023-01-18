package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.speed.SpeedRecord;
import com.zhangqiang.downloadmanager.task.speed.SpeedSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DownloadTask implements SpeedSupport {

    private final DownloadListeners downloadListeners = new DownloadListeners();
    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final SpeedRecord speedRecord = new SpeedRecord();
    private final String id;

    public DownloadTask(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    protected abstract void onStart();

    protected abstract void onCancel();

    @Override
    public abstract long getCurrentLength();

    public final boolean isStarted(){
        return mStarted.get();
    }

    public void reset() {
        cancel();
        getDownloadListeners().notifyIdle();
    }

    public final void start() {
        if (mStarted.getAndSet(true)) {
            return;
        }
        onStart();
        getDownloadListeners().notifyStart();
    }

    public final void cancel() {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        onCancel();
        getDownloadListeners().notifyCancel();
    }

    protected void dispatchComplete() {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        getDownloadListeners().notifyComplete();
    }

    protected void dispatchFail(DownloadException e) {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        getDownloadListeners().notifyFail(e);
    }

    public List<? extends DownloadTask> getChildTasks() {
        return null;
    }

    public List<String> getFilePaths() {
        return null;
    }

    public interface DownloadListener {

        void onIdle();

        void onStart();

        void onComplete();

        void onFail(DownloadException e);

        void onCancel();
    }

    public void addDownloadListener(DownloadListener downloadListener) {
        getDownloadListeners().addDownloadListener(downloadListener);
    }

    public void removeDownloadListener(DownloadListener downloadListener) {
        getDownloadListeners().removeDownloadListener(downloadListener);
    }

    @Override
    public SpeedRecord getSpeedRecord() {
        return speedRecord;
    }

    public DownloadListeners getDownloadListeners() {
        return downloadListeners;
    }
}

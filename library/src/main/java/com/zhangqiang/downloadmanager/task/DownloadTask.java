package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.speed.SpeedRecord;
import com.zhangqiang.downloadmanager.task.speed.SpeedSupport;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DownloadTask implements SpeedSupport {

    private final DownloadListeners downloadListeners = new DownloadListeners();
    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final SpeedRecord speedRecord = new SpeedRecord();

    protected abstract void onStart();

    protected abstract void onCancel();

    @Override
    public abstract long getCurrentLength();

    public final boolean isStarted(){
        return mStarted.get();
    }

    public void reset() {
        cancel();
        getDownloadListeners().notifyReset();
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

    public interface DownloadListener {

        void onReset();

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

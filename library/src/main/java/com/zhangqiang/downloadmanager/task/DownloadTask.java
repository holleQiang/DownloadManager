package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.exception.DownloadException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class DownloadTask {

    private List<DownloadListener> downloadListeners;
    private final AtomicBoolean mStarted = new AtomicBoolean(false);

    protected abstract void onStart();

    protected abstract void onCancel();

    public boolean isStarted() {
        return mStarted.get();
    }

    public abstract long getCurrentLength();

    public final void start() {
        if (mStarted.getAndSet(true)) {
            return;
        }
        notifyStart();
        onStart();
    }

    public final void cancel() {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        notifyCancel();
        onCancel();
    }

    protected void dispatchComplete() {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        notifyComplete();
    }

    protected void dispatchFail(DownloadException e) {
        if (!mStarted.getAndSet(false)) {
            return;
        }
        notifyFail(e);
    }

    private synchronized void notifyStart() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onStart();
        }
    }

    private synchronized void notifyComplete() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onComplete();
        }
    }

    private synchronized void notifyFail(DownloadException e) {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onFail(e);
        }
    }

    private synchronized void notifyCancel() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onCancel();
        }
    }


    public interface DownloadListener {

        void onStart();

        void onComplete();

        void onFail(DownloadException e);

        void onCancel();
    }

    public synchronized void addDownloadListener(DownloadListener downloadListener) {
        if (downloadListeners == null) {
            downloadListeners = new ArrayList<>();
        }
        if (downloadListeners.contains(downloadListener)) {
            return;
        }
        downloadListeners.add(downloadListener);
    }

    public synchronized void removeDownloadListener(DownloadListener downloadListener) {
        if (downloadListeners == null) {
            return;
        }
        downloadListeners.remove(downloadListener);
    }
}

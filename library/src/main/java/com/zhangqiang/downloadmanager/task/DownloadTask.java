package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.exception.DownloadException;

import java.util.ArrayList;
import java.util.List;

public abstract class DownloadTask {

    private List<DownloadListener> downloadListeners;

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

    protected abstract void onStart();

    protected abstract void onCancel();

    public abstract boolean isRunning();

    public final void start() {
        onStart();
    }

    public final void cancel() {
        onCancel();
    }

    protected synchronized void notifyComplete() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onComplete();
        }
    }

    protected synchronized void notifyFail(DownloadException e) {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onFail(e);
        }
    }

    protected synchronized void notifyCancel() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onCancel();
        }
    }

    protected synchronized void notifyStart() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onStart();
        }
    }

    public interface DownloadListener {

        void onStart();

        void onComplete();

        void onFail(DownloadException e);

        void onCancel();
    }
}

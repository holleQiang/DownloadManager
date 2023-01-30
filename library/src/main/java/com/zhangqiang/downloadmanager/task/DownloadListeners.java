package com.zhangqiang.downloadmanager.task;

import com.zhangqiang.downloadmanager.exception.DownloadException;

import java.util.ArrayList;
import java.util.List;

public class DownloadListeners {

    private List<DownloadTask.DownloadListener> downloadListeners;

    public synchronized void notifyReset() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onReset();
        }
    }

    public synchronized void notifyStart() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onStart();
        }
    }

    public synchronized void notifyComplete() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onComplete();
        }
    }

    public synchronized void notifyFail(DownloadException e) {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onFail(e);
        }
    }

    public synchronized void notifyCancel() {
        if (downloadListeners == null) {
            return;
        }
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onCancel();
        }
    }

    public synchronized void addDownloadListener(DownloadTask.DownloadListener downloadListener) {
        if (downloadListeners == null) {
            downloadListeners = new ArrayList<>();
        }
        if (downloadListeners.contains(downloadListener)) {
            return;
        }
        downloadListeners.add(downloadListener);
    }

    public synchronized void removeDownloadListener(DownloadTask.DownloadListener downloadListener) {
        if (downloadListeners == null) {
            return;
        }
        downloadListeners.remove(downloadListener);
    }
}

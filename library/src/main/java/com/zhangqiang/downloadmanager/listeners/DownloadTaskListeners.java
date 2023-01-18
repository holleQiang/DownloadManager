package com.zhangqiang.downloadmanager.listeners;

import java.util.ArrayList;
import java.util.List;

public class DownloadTaskListeners {
    private List<DownloadTaskListener> downloadTaskListeners;

    public synchronized void notifyTaskStateChanged(String id){
        if (downloadTaskListeners != null) {
            for (int i = 0; i < downloadTaskListeners.size(); i++) {
                downloadTaskListeners.get(i).onTaskStateChanged(id);
            }
        }
    }

    public synchronized void notifyTaskInfoChanged(String id) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskInfoChanged(id);
            }
        }
    }

    public synchronized void notifyTaskProgressChanged(String id){
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskProgressChanged(id);
            }
        }
    }

    public synchronized void addDownloadTaskListener(DownloadTaskListener listener) {
        if (downloadTaskListeners == null) {
            downloadTaskListeners = new ArrayList<>();
        }
        if (downloadTaskListeners.contains(listener)) {
            return;
        }
        downloadTaskListeners.add(listener);
    }

    public synchronized void removeDownloadTaskListener(DownloadTaskListener listener) {
        if (downloadTaskListeners == null) {
            return;
        }
        downloadTaskListeners.remove(listener);
    }

    public synchronized void notifyTaskSpeedChanged(String id) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskSpeedChanged(id);
            }
        }
    }

    public synchronized void notifyTaskRemoved(String id) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskRemoved(id);
            }
        }
    }

    public synchronized void notifyTaskAdded(String id) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskAdded(id);
            }
        }
    }

    public synchronized void notifyActiveTaskSizeChanged() {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onActiveTaskSizeChanged();
            }
        }
    }

}

package com.zhangqiang.downloadmanager.manager;

import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.plugin.DownloadPlugin;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager {

    private static final DownloadManager instance = new DownloadManager();
    private final List<DownloadPlugin> downloadPlugins = new ArrayList<>();
    private final List<DownloadTaskFactory> downloadTaskFactories = new ArrayList<>();
    private final List<DownloadTask> downloadTasks = new ArrayList<>();
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final List<OnTaskCountChangeListener> onTaskCountChangeListeners = new ArrayList<>();
    private final List<OnActiveTaskCountChangeListener> onActiveTaskCountChangeListeners = new ArrayList<>();
    private final List<OnDownloadTaskDeleteListener> onDownloadTaskDeleteListeners = new ArrayList<>();

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        return instance;
    }

    public void registerPlugin(DownloadPlugin plugin) {
        downloadPlugins.add(plugin);
        plugin.apply(this);
    }

    public void unRegisterPlugin(DownloadPlugin plugin) {
        downloadPlugins.remove(plugin);
        plugin.drop();
    }

    public int getPluginCount() {
        return downloadPlugins.size();
    }

    public DownloadPlugin getPluginAt(int position) {
        return downloadPlugins.get(position);
    }

    public DownloadTask enqueue(DownloadRequest request) {
        synchronized (downloadTasks) {
            for (int i = 0; i < downloadTaskFactories.size(); i++) {
                DownloadTask downloadTask = downloadTaskFactories.get(i).createTask(request);
                if (downloadTask != null) {
                    configDownloadTask(downloadTask);
                    downloadTasks.add(downloadTask);
                    sortTasks();
                    dispatchTaskCountChange(downloadTasks.size(), downloadTasks.size() - 1);
                    downloadTask.start();
                    return downloadTask;
                }
            }
            throw new IllegalArgumentException("cannot find download task factory");
        }
    }

    private void configDownloadTask(DownloadTask downloadTask) {
        downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.DOWNLOADING) {
                    int curr = activeTaskCount.incrementAndGet();
                    dispatchActiveTaskCountChange(curr, curr - 1);
                } else {
                    int curr = activeTaskCount.decrementAndGet();
                    dispatchActiveTaskCountChange(curr, curr + 1);
                }
            }
        });
    }

    private void sortTasks() {
        synchronized (downloadTasks) {
            Collections.sort(downloadTasks, new Comparator<DownloadTask>() {
                @Override
                public int compare(DownloadTask o1, DownloadTask o2) {
                    return Long.compare(o2.getCreateTime(), o1.getCreateTime());
                }
            });
        }
    }

    public synchronized void addDownloadTasks(List<? extends DownloadTask> tasks) {
        synchronized (downloadTasks) {
            if (tasks == null || tasks.isEmpty()) {
                return;
            }
            for (DownloadTask downloadTask : tasks) {
                configDownloadTask(downloadTask);
            }
            final int oldCount = getTaskCount();
            this.downloadTasks.addAll(tasks);
            sortTasks();
            dispatchTaskCountChange(getTaskCount(), oldCount);
        }
    }

    public void deleteTask(DownloadTask task, RemoveTaskOptions options) {
        synchronized (downloadTasks) {
            if (downloadTasks.remove(task)) {
                if(task.getStatus() == Status.DOWNLOADING){
                    task.cancel();
                }
                if (options.isDeleteFile()) {
                    try {
                        FileUtils.deleteFileOrThrow(new File(task.getSaveDir(), task.getSaveFileName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                dispatchTaskCountChange(downloadTasks.size(), downloadTasks.size() + 1);
                dispatchDownloadTaskDelete(task);
            }
        }
    }

    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    private void dispatchTaskCountChange(int newCount, int oldCount) {
        synchronized (onTaskCountChangeListeners) {
            for (int i = onTaskCountChangeListeners.size() - 1; i >= 0; i--) {
                onTaskCountChangeListeners.get(i).onTaskCountChange(newCount, oldCount);
            }
        }
    }

    private void dispatchActiveTaskCountChange(int newCount, int oldCount) {
        synchronized (onActiveTaskCountChangeListeners) {
            for (int i = onActiveTaskCountChangeListeners.size() - 1; i >= 0; i--) {
                onActiveTaskCountChangeListeners.get(i).onActiveTaskCountChange(newCount, oldCount);
            }
        }
    }

    public void addDownloadTaskFactory(DownloadTaskFactory factory) {
        downloadTaskFactories.add(factory);
    }

    public DownloadTask getTask(int index) {
        synchronized (downloadTasks) {
            return downloadTasks.get(index);
        }
    }

    public int getTaskCount() {
        synchronized (downloadTasks) {
            return downloadTasks.size();
        }
    }

    public void addTaskCountChangeListener(OnTaskCountChangeListener listener) {
        synchronized (onTaskCountChangeListeners) {
            onTaskCountChangeListeners.add(listener);
        }
    }

    public void removeTaskCountChangeListener(OnTaskCountChangeListener listener) {
        synchronized (onTaskCountChangeListeners) {
            onTaskCountChangeListeners.remove(listener);
        }
    }

    public void addActiveTaskCountChangeListener(OnActiveTaskCountChangeListener listener) {
        synchronized (onActiveTaskCountChangeListeners) {
            onActiveTaskCountChangeListeners.add(listener);
        }
    }

    public void removeActiveTaskCountChangeListener(OnActiveTaskCountChangeListener listener) {
        synchronized (onActiveTaskCountChangeListeners) {
            onActiveTaskCountChangeListeners.remove(listener);
        }
    }

    public void addOnDownloadTaskDeleteListener(OnDownloadTaskDeleteListener listener){
        synchronized (onDownloadTaskDeleteListeners) {
            onDownloadTaskDeleteListeners.add(listener);
        }
    }

    public void removeOnDownloadTaskDeleteListener(OnDownloadTaskDeleteListener listener){
        synchronized (onDownloadTaskDeleteListeners) {
            onDownloadTaskDeleteListeners.remove(listener);
        }
    }

    private void dispatchDownloadTaskDelete(DownloadTask downloadTask){
        synchronized (onDownloadTaskDeleteListeners) {
            for (int i = onDownloadTaskDeleteListeners.size() - 1; i >= 0; i--) {
                onDownloadTaskDeleteListeners.get(i).onDownloadTaskDelete(downloadTask);
            }
        }
    }
}

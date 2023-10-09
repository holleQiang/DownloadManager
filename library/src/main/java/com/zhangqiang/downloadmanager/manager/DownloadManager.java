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

public class DownloadManager {

    private final List<DownloadPlugin> downloadPlugins = new ArrayList<>();
    private final List<DownloadTaskFactory> downloadTaskFactories = new ArrayList<>();
    private final List<DownloadTask> downloadTasks = new ArrayList<>();
    private int activeTaskCount = 0;
    private final List<OnTaskCountChangeListener> onTaskCountChangeListeners = new ArrayList<>();
    private final List<OnActiveTaskCountChangeListener> onActiveTaskCountChangeListeners = new ArrayList<>();

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

    private void configDownloadTask(DownloadTask downloadTask) {
        downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.DOWNLOADING) {
                    activeTaskCount++;
                    dispatchActiveTaskCountChange(activeTaskCount, activeTaskCount - 1);
                } else {
                    activeTaskCount--;
                    dispatchActiveTaskCountChange(activeTaskCount, activeTaskCount + 1);
                }
            }
        });
    }

    private void sortTasks() {
        Collections.sort(downloadTasks, new Comparator<DownloadTask>() {
            @Override
            public int compare(DownloadTask o1, DownloadTask o2) {
                return Long.compare(o2.getCreateTime(), o1.getCreateTime());
            }
        });
    }

    public synchronized void addDownloadTasks(List<? extends DownloadTask> tasks) {
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

    public void deleteTask(DownloadTask task, RemoveTaskOptions options) {
        if (downloadTasks.remove(task)) {
            task.cancel();
            if (options.isDeleteFile()) {
                try {
                    FileUtils.deleteFileIfExists(new File(task.getSaveDir(), task.getSaveFileName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dispatchTaskCountChange(downloadTasks.size(), downloadTasks.size() + 1);
        }
    }

    public int getActiveTaskCount() {
        return activeTaskCount;
    }

    private void dispatchTaskCountChange(int newCount, int oldCount) {
        for (int i = onTaskCountChangeListeners.size() - 1; i >= 0; i--) {
            onTaskCountChangeListeners.get(i).onTaskCountChange(newCount, oldCount);
        }
    }

    private void dispatchActiveTaskCountChange(int newCount, int oldCount) {
        for (int i = onActiveTaskCountChangeListeners.size() - 1; i >= 0; i--) {
            onActiveTaskCountChangeListeners.get(i).onActiveTaskCountChange(newCount, oldCount);
        }
    }

    public void addDownloadTaskFactory(DownloadTaskFactory factory) {
        downloadTaskFactories.add(factory);
    }

    public DownloadTask getTask(int index) {
        return downloadTasks.get(index);
    }

    public int getTaskCount() {
        return downloadTasks.size();
    }

    public void addTaskCountChangeListener(OnTaskCountChangeListener listener) {
        onTaskCountChangeListeners.add(listener);
    }

    public void removeTaskCountChangeListener(OnTaskCountChangeListener listener) {
        onTaskCountChangeListeners.remove(listener);
    }

    public void addActiveTaskCountChangeListener(OnActiveTaskCountChangeListener listener) {
        onActiveTaskCountChangeListeners.add(listener);
    }

    public void removeActiveTaskCountChangeListener(OnActiveTaskCountChangeListener listener) {
        onActiveTaskCountChangeListeners.remove(listener);
    }
}

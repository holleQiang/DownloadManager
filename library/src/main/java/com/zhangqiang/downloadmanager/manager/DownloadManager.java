package com.zhangqiang.downloadmanager.manager;

import com.zhangqiang.downloadmanager.plugin.retry.RetryPlugin;
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
    private final List<OnTaskAddedListener> onTaskAddedListeners = new ArrayList<>();
    private final AtomicInteger maxActiveTaskSize = new AtomicInteger(Integer.MAX_VALUE);

    private DownloadManager() {
        //默认注册重试插件
        registerPlugin(new RetryPlugin());
    }

    public static DownloadManager getInstance() {
        return instance;
    }

    public void registerPlugin(DownloadPlugin plugin) {
        synchronized (downloadPlugins) {
            downloadPlugins.add(plugin);
            plugin.apply(this);
        }
    }

    public void unRegisterPlugin(DownloadPlugin plugin) {
        synchronized (downloadPlugins) {
            downloadPlugins.remove(plugin);
            plugin.drop(this);
        }
    }

    public int getPluginCount() {
        synchronized (downloadPlugins) {
            return downloadPlugins.size();
        }
    }

    public DownloadPlugin getPluginAt(int position) {
        synchronized (downloadPlugins) {
            return downloadPlugins.get(position);
        }
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
                    dispatchTaskAdded(Collections.singletonList(downloadTask));
                    startMaxPriorityIdlTasks();
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
            final int oldCount = getTaskCount();
            this.downloadTasks.addAll(tasks);
            sortTasks();
            dispatchTaskCountChange(getTaskCount(), oldCount);
            dispatchTaskAdded(new ArrayList<>(tasks));
            int downloadingTaskCount = 0;
            for (DownloadTask downloadTask : tasks) {
                configDownloadTask(downloadTask);
                if (downloadTask.getStatus() == Status.DOWNLOADING) {
                    downloadingTaskCount++;
                    downloadTask.forceStart();
                }
            }
            if (downloadingTaskCount > 0) {
                int oldActiveCount = activeTaskCount.getAndAdd(downloadingTaskCount);
                dispatchActiveTaskCountChange(oldActiveCount + downloadingTaskCount, oldActiveCount);
            }
        }
    }

    public void deleteTask(DownloadTask task, RemoveTaskOptions options) {
        synchronized (downloadTasks) {
            if (downloadTasks.remove(task)) {
                if (task.getStatus() == Status.DOWNLOADING) {
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

    public void addOnDownloadTaskDeleteListener(OnDownloadTaskDeleteListener listener) {
        synchronized (onDownloadTaskDeleteListeners) {
            onDownloadTaskDeleteListeners.add(listener);
        }
    }

    public void removeOnDownloadTaskDeleteListener(OnDownloadTaskDeleteListener listener) {
        synchronized (onDownloadTaskDeleteListeners) {
            onDownloadTaskDeleteListeners.remove(listener);
        }
    }

    private void dispatchDownloadTaskDelete(DownloadTask downloadTask) {
        synchronized (onDownloadTaskDeleteListeners) {
            for (int i = onDownloadTaskDeleteListeners.size() - 1; i >= 0; i--) {
                onDownloadTaskDeleteListeners.get(i).onDownloadTaskDelete(downloadTask);
            }
        }
    }

    public void setMaxActiveTaskSize(int maxSize) {
        maxActiveTaskSize.set(maxSize);
        int activeTaskCount = getActiveTaskCount();
        if (maxSize < activeTaskCount) {
            cancelMinPriorityDownloadTasks();
        } else if (maxSize > activeTaskCount) {
            startMaxPriorityIdlTasks();
        }
    }

    public int getMaxActiveTaskSize() {
        return maxActiveTaskSize.get();
    }


    private DownloadTask findMaxPriorityIdlTask() {
        synchronized (downloadTasks) {
            int maxPriority = Integer.MIN_VALUE;
            DownloadTask maxPriorityTask = null;
            for (DownloadTask downloadTask : downloadTasks) {
                if (downloadTask.getStatus() == Status.IDLE) {
                    int priority = downloadTask.getPriority();
                    if (priority > maxPriority) {
                        maxPriority = priority;
                        maxPriorityTask = downloadTask;
                    }
                }
            }
            return maxPriorityTask;
        }
    }

    private void startMaxPriorityIdlTasks() {
        int activeTaskCount = getActiveTaskCount();
        int maxActiveTaskSize = getMaxActiveTaskSize();
        if (activeTaskCount >= maxActiveTaskSize) {
            return;
        }
        int index = 0;
        while (index < maxActiveTaskSize - activeTaskCount) {
            DownloadTask maxPriorityIdlTask = findMaxPriorityIdlTask();
            if (maxPriorityIdlTask == null) {
                break;
            }
            maxPriorityIdlTask.start();
            index++;
        }
    }

    private void cancelMinPriorityDownloadTasks() {
        int activeTaskCount = getActiveTaskCount();
        int maxActiveTaskSize = getMaxActiveTaskSize();
        if (activeTaskCount <= maxActiveTaskSize) {
            return;
        }
        int index = 0;
        while (index < activeTaskCount - maxActiveTaskSize) {
            DownloadTask maxPriorityIdlTask = findMinPriorityDownloadingTask();
            if (maxPriorityIdlTask == null) {
                break;
            }
            maxPriorityIdlTask.cancel();
            index++;
        }
    }

    private DownloadTask findMinPriorityDownloadingTask() {
        synchronized (downloadTasks) {
            int minPriority = Integer.MAX_VALUE;
            DownloadTask minPriorityTask = null;
            for (DownloadTask downloadTask : downloadTasks) {
                if (downloadTask.getStatus() == Status.DOWNLOADING) {
                    int priority = downloadTask.getPriority();
                    if (priority < minPriority) {
                        minPriority = priority;
                        minPriorityTask = downloadTask;
                    }
                }
            }
            return minPriorityTask;
        }
    }

    public void addOnTaskAddedListener(OnTaskAddedListener listener) {
        synchronized (onTaskAddedListeners) {
            onTaskAddedListeners.add(listener);
        }
    }

    public void removeOnTaskAddedListener(OnTaskAddedListener listener) {
        synchronized (onTaskAddedListeners) {
            onTaskAddedListeners.remove(listener);
        }
    }

    private void dispatchTaskAdded(List<DownloadTask> tasks) {
        synchronized (onTaskAddedListeners) {
            for (int i = onTaskAddedListeners.size() - 1; i >= 0; i--) {
                onTaskAddedListeners.get(i).onTaskAdded(tasks);
            }
        }
    }
}

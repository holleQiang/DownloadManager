package com.zhangqiang.downloadmanager.plugin.limit;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.OnTaskAddedListener;
import com.zhangqiang.downloadmanager.manager.OnTaskCountChangeListener;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.task.interceptor.Chain;
import com.zhangqiang.downloadmanager.task.interceptor.Interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ActiveTaskCountLimitPlugin extends SimpleDownloadPlugin {

    private final AtomicInteger activeTaskCount = new AtomicInteger(0);
    private final List<OnActiveTaskCountChangeListener> onActiveTaskCountChangeListeners = new ArrayList<>();
    private final AtomicInteger maxActiveTaskCount = new AtomicInteger(Integer.MAX_VALUE);

    @Override
    protected void onApply(DownloadManager downloadManager) {
        super.onApply(downloadManager);
//        downloadManager.addTaskCountChangeListener(new OnTaskCountChangeListener() {
//            @Override
//            public void onTaskCountChange(int newCount, int oldCount) {
//                tryStartIdleTask();
//                tryCancelDownloadingTask();
//            }
//        });
        downloadManager.addOnTaskAddedListener(new OnTaskAddedListener() {
            @Override
            public void onTaskAdded(List<DownloadTask> tasks) {
                int downloadingTaskCount = 0;
                for (DownloadTask downloadTask : tasks) {
                    if (downloadTask.getStatus() == Status.DOWNLOADING) {
                        downloadingTaskCount++;
                    }
                    downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
                        @Override
                        public void onStatusChange(Status newStatus, Status oldStatus) {
                            if (newStatus == Status.DOWNLOADING) {
                                int curr = activeTaskCount.incrementAndGet();
                                dispatchActiveTaskCountChange(curr, curr - 1);
                                tryCancelDownloadingTask();
                            } else {
                                int curr = activeTaskCount.decrementAndGet();
                                dispatchActiveTaskCountChange(curr, curr + 1);
                                tryStartIdleTask();
                            }
                        }
                    });
                    downloadTask.addStartInterceptor(new Interceptor() {
                        @Override
                        public void onIntercept(Chain chain) {
                            if(getActiveTaskCount() >= getMaxActiveTaskCount()){
                                return;
                            }
                            chain.proceed();
                        }
                    });
                }
                if (downloadingTaskCount > 0) {
                    int oldActiveCount = activeTaskCount.getAndAdd(downloadingTaskCount);
                    dispatchActiveTaskCountChange(oldActiveCount + downloadingTaskCount, oldActiveCount);
                }
            }
        });
    }


    private void tryStartIdleTask() {
        DownloadManager downloadManager = getDownloadManager();
        if (downloadManager == null) {
            return;
        }
        int taskCount = downloadManager.getTaskCount();
        for (int i = 0; i < taskCount; i++) {
            if (getActiveTaskCount() >= getMaxActiveTaskCount()) {
                break;
            }
            DownloadTask downloadTask = downloadManager.getTask(i);
            if (downloadTask.getStatus() == Status.IDLE) {
                downloadTask.start();
            }
        }
    }

    private void tryCancelDownloadingTask() {
        DownloadManager downloadManager = getDownloadManager();
        if (downloadManager == null) {
            return;
        }
        int taskCount = downloadManager.getTaskCount();
        for (int i = 0; i < taskCount; i++) {
            if (getActiveTaskCount() <= getMaxActiveTaskCount()) {
                break;
            }
            DownloadTask downloadTask = downloadManager.getTask(i);
            if (downloadTask.getStatus() == Status.DOWNLOADING) {
                downloadTask.cancel();
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

    public void setMaxActiveTaskCount(int maxSize) {
        maxActiveTaskCount.set(maxSize);
        int activeTaskCount = getActiveTaskCount();
        if (activeTaskCount < maxSize) {
            tryStartIdleTask();
        } else if (activeTaskCount > maxSize) {
            tryCancelDownloadingTask();
        }
    }


    public int getMaxActiveTaskCount() {
        return maxActiveTaskCount.get();
    }

    public int getActiveTaskCount() {
        return activeTaskCount.get();
    }

    @Override
    public String getName() {
        return "下载任务数量限制插件";
    }
}

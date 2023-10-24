package com.zhangqiang.downloadmanager.manager;

import com.zhangqiang.downloadmanager.manager.interceptor.enqueue.Chain;
import com.zhangqiang.downloadmanager.manager.interceptor.enqueue.EnqueueInterceptor;
import com.zhangqiang.downloadmanager.manager.interceptor.enqueue.RealEnqueueChain;
import com.zhangqiang.downloadmanager.plugin.limit.OnActiveTaskCountChangeListener;
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
    private final List<OnTaskCountChangeListener> onTaskCountChangeListeners = new ArrayList<>();
    private final List<OnDownloadTaskDeleteListener> onDownloadTaskDeleteListeners = new ArrayList<>();
    private final List<OnTaskAddedListener> onTaskAddedListeners = new ArrayList<>();
    private final List<EnqueueInterceptor> enqueueInterceptors = new ArrayList<>();

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
            plugin.drop();
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
        synchronized (enqueueInterceptors) {
            List<EnqueueInterceptor> interceptors = new ArrayList<>();
            interceptors.add(new RealEnqueueInterceptor());
            return new RealEnqueueChain(request,interceptors,0).proceed(request);
        }
    }

    private final class RealEnqueueInterceptor implements EnqueueInterceptor{

        @Override
        public DownloadTask onIntercept(Chain chain) {
            synchronized (downloadTasks) {
                for (int i = 0; i < downloadTaskFactories.size(); i++) {
                    DownloadTask downloadTask = downloadTaskFactories.get(i).createTask(chain.getRequest());
                    if (downloadTask != null) {
                        configDownloadTask(downloadTask);
                        downloadTasks.add(downloadTask);
                        sortTasks();
                        dispatchTaskCountChange(downloadTasks.size(), downloadTasks.size() - 1);
                        dispatchTaskAdded(Collections.singletonList(downloadTask));
                        downloadTask.start();
                        return downloadTask;
                    }
                }
                throw new IllegalArgumentException("cannot find download task factory");
            }
        }
    }



    private void configDownloadTask(DownloadTask downloadTask) {
        downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {

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
            for (DownloadTask downloadTask : tasks) {
                configDownloadTask(downloadTask);
                if (downloadTask.getStatus() == Status.DOWNLOADING) {
                    downloadTask.forceStart();
                }
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


    private void dispatchTaskCountChange(int newCount, int oldCount) {
        synchronized (onTaskCountChangeListeners) {
            for (int i = onTaskCountChangeListeners.size() - 1; i >= 0; i--) {
                onTaskCountChangeListeners.get(i).onTaskCountChange(newCount, oldCount);
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

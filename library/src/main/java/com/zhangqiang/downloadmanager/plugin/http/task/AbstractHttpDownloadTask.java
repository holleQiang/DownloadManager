package com.zhangqiang.downloadmanager.plugin.http.task;

import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.Status;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHttpDownloadTask extends DownloadTask {

    private final String url;
    private ResourceInfo resourceInfo;
    private final List<OnResourceInfoReadyListener> onResourceInfoReadyListeners = new ArrayList<>();

    public AbstractHttpDownloadTask(String id, String saveDir, String targetFileName, long createTime, int priority, String url) {
        super(id, saveDir, targetFileName, createTime, priority);
        this.url = url;
    }

    public AbstractHttpDownloadTask(String id, String saveDir, String targetFileName, long createTime, int priority, Status status, String errorMessage, long currentLength, String url, ResourceInfo resourceInfo) {
        super(id, saveDir, targetFileName, createTime, priority, status, errorMessage, currentLength);
        this.url = url;
        this.resourceInfo = resourceInfo;
    }

    public void addOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.add(listener);
    }

    public void removeOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.remove(listener);
    }

    protected void dispatchResourceInfoReady(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        for (int i = onResourceInfoReadyListeners.size() - 1; i >= 0; i--) {
            onResourceInfoReadyListeners.get(i).onResourceInfoReady(resourceInfo);
        }
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public String getUrl() {
        return url;
    }
}

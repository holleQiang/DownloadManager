package com.zhangqiang.downloadmanager.task.http.support;

import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpTaskBean;
import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.speed.SpeedUtils;

import java.util.List;

public class TaskInfoImpl implements TaskInfo {

    private final HttpTaskBean httpTaskBean;
    private final DownloadTask downloadTask;

    public TaskInfoImpl(HttpTaskBean httpTaskBean, DownloadTask downloadTask) {
        this.httpTaskBean = httpTaskBean;
        this.downloadTask = downloadTask;
    }

    @Override
    public String getId() {
        return httpTaskBean.getId();
    }

    @Override
    public String getUrl() {
        return httpTaskBean.getUrl();
    }

    @Override
    public String getSaveDir() {
        return httpTaskBean.getSaveDir();
    }

    @Override
    public String getFileName() {
        return httpTaskBean.getFileName();
    }

    @Override
    public long getCurrentLength() {
        int type = httpTaskBean.getType();
        if (type == HttpTaskBean.TYPE_DEFAULT) {
            return httpTaskBean.getHttpDefaultTask().getCurrentLength();
        } else if (type == HttpTaskBean.TYPE_PART) {
            HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
            if(httpPartTask != null){
                long length = 0;
                List<HttpPartTaskItemBean> items = httpPartTask.getItems();
                if (items != null) {
                    for (HttpPartTaskItemBean item : items) {
                        length += (item.getCurrentPosition() - item.getStartPosition());
                    }
                }
                return length;
            }
        }
        return 0;
    }

    @Override
    public long getContentLength() {
        return httpTaskBean.getContentLength();
    }

    @Override
    public int getState() {
        int state = httpTaskBean.getState();
        if (state == HttpTaskBean.STATE_IDLE) {
            return STATE_IDLE;
        } else if (state == HttpTaskBean.STATE_START
                || state == HttpTaskBean.STATE_GENERATING_INFO
                || state == HttpTaskBean.STATE_WAITING_CHILDREN_TASK) {
            return STATE_DOWNLOADING;
        } else if (state == HttpTaskBean.STATE_SUCCESS) {
            return STATE_COMPLETE;
        } else if (state == HttpTaskBean.STATE_FAIL) {
            return STATE_FAIL;
        } else if (state == HttpTaskBean.STATE_CANCEL) {
            return STATE_PAUSE;
        }
        return 0;
    }

    @Override
    public String getContentType() {
        return httpTaskBean.getContentType();
    }

    @Override
    public long getCreateTime() {
        return httpTaskBean.getCreateTime().getTime();
    }

    @Override
    public String getErrorMsg() {
        return httpTaskBean.getErrorMsg();
    }

    @Override
    public int getThreadCount() {
        return httpTaskBean.getThreadSize();
    }

    @Override
    public long getSpeed() {
        return SpeedUtils.getSpeed(downloadTask);
    }

    @Override
    public int getPartCount() {
        HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
        if (httpPartTask != null) {
            List<HttpPartTaskItemBean> taskItems = httpPartTask.getItems();
            return taskItems != null ? taskItems.size() : 0;
        }
        return 0;
    }

    @Override
    public long getPartSpeed(int partIndex) {
        List<HttpDownloadPartTask> partTasks = ((HttpDownloadTask) downloadTask).getPartTasks();
        if (partTasks != null && partIndex < partTasks.size()) {
            return SpeedUtils.getSpeed(partTasks.get(partIndex));
        }
        return 0;
    }

    @Override
    public long getPartCurrentLength(int partIndex) {
        HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
        if (httpPartTask != null) {
            List<HttpPartTaskItemBean> taskItems = httpPartTask.getItems();
            if (taskItems != null && partIndex < taskItems.size()) {
                HttpPartTaskItemBean taskItemBean = taskItems.get(partIndex);
                return taskItemBean.getCurrentPosition() - taskItemBean.getStartPosition();
            }
        }
        return 0;
    }

    @Override
    public long getPartContentLength(int partIndex) {
        HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
        if (httpPartTask != null) {
            List<HttpPartTaskItemBean> taskItems = httpPartTask.getItems();
            if (taskItems != null && partIndex < taskItems.size()) {
                HttpPartTaskItemBean taskItemBean = taskItems.get(partIndex);
                return taskItemBean.getEndPosition() - taskItemBean.getStartPosition();
            }
        }
        return 0;
    }
}

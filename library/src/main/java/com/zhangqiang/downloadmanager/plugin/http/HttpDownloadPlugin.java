package com.zhangqiang.downloadmanager.plugin.http;

import android.content.Context;

import com.zhangqiang.downloadmanager.plugin.http.bean.HttpDefaultTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskItemService;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskService;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpTaskService;
import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.DownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.OnHttpPartDownloadTasksReadyListener;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.plugin.http.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.http.task.ResourceInfo;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;
import com.zhangqiang.downloadmanager.task.OnSaveFileNameChangeListener;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.OnTaskFailListener;
import com.zhangqiang.downloadmanager.task.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HttpDownloadPlugin implements DownloadPlugin {

    private final Context context;
    private final HttpTaskService httpTaskService;
    private final HttpPartTaskService httpPartTaskService;
    private final HttpPartTaskItemService httpPartTaskItemService;

    public HttpDownloadPlugin(Context context) {
        this.context = context;
        this.httpTaskService = new HttpTaskService(context);
        this.httpPartTaskService = new HttpPartTaskService(context);
        this.httpPartTaskItemService = new HttpPartTaskItemService(context);
    }

    @Override
    public void apply(DownloadManager downloadManager) {
        downloadManager.addDownloadTaskFactory(new HttpDownloadTaskFactory());
        List<HttpDownloadTask> httpDownloadTasks = new ArrayList<>();
        List<HttpTaskBean> httpTasks = httpTaskService.getHttpTasks();
        for (int i = 0; i < httpTasks.size(); i++) {
            HttpTaskBean httpTaskBean = httpTasks.get(i);
            int state = httpTaskBean.getState();
            HttpDownloadTask httpDownloadTask;
            if (state == HttpTaskBean.STATE_IDLE) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getUrl(),
                        context,
                        httpTaskBean.getThreadSize());
            } else if (state == HttpTaskBean.STATE_START
                    || state == HttpTaskBean.STATE_GENERATING_INFO
                    || state == HttpTaskBean.STATE_WAITING_CHILDREN_TASK) {

                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName()
                );
                httpDownloadTask.forceStart();
            } else if (state == HttpTaskBean.STATE_SUCCESS) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        Status.SUCCESS,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else if (state == HttpTaskBean.STATE_FAIL) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        Status.FAIL,
                        httpTaskBean.getErrorMsg(),
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else if (state == HttpTaskBean.STATE_CANCEL) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        Status.CANCELED,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else {
                throw new IllegalArgumentException("unknown http task bean state:" + state);
            }
            handleDownloadTaskSave(httpDownloadTask, httpTaskBean);
            httpDownloadTasks.add(httpDownloadTask);
        }
        downloadManager.addDownloadTasks(httpDownloadTasks);
    }

    @Override
    public void drop() {

    }

    @Override
    public String getName() {
        return "Http协议下载插件";
    }

    private static long getCurrentLength(HttpTaskBean httpTaskBean) {
        int type = httpTaskBean.getType();
        if (type == HttpTaskBean.TYPE_DEFAULT) {
            HttpDefaultTaskBean httpDefaultTask = httpTaskBean.getHttpDefaultTaskBean();
            if (httpDefaultTask != null) {
                return httpDefaultTask.getCurrentLength();
            }
        } else if (type == HttpTaskBean.TYPE_PART) {
            HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTaskBean();
            if (httpPartTask != null) {
                List<HttpPartTaskItemBean> items = httpPartTask.getItems();
                long length = 0;
                for (int i = 0; i < items.size(); i++) {
                    HttpPartTaskItemBean taskItemBean = items.get(i);
                    length += taskItemBean.getCurrentLength();
                }
                return length;
            }
        }
        return 0;
    }

    private static ResourceInfo makeResourceInfo(HttpTaskBean httpTaskBean) {
        return new ResourceInfo(httpTaskBean.getFileName(),
                httpTaskBean.getContentLength(),
                httpTaskBean.getContentType(),
                httpTaskBean.getResponseCode());
    }

    private List<HttpPartDownloadTask> makePartDownloadTasks(HttpTaskBean httpTaskBean, Context context) {
        HttpPartTaskBean httpPartTaskBean = httpTaskBean.getHttpPartTaskBean();
        if (httpPartTaskBean == null) {
            return null;
        }
        List<HttpPartTaskItemBean> items = httpPartTaskBean.getItems();
        List<HttpPartDownloadTask> partDownloadTasks = new ArrayList<>();
        for (HttpPartTaskItemBean item : items) {
            int state = item.getState();
            HttpPartDownloadTask partDownloadTask;
            if (state == HttpPartTaskItemBean.STATE_IDLE) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.IDLE,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_START) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        null,
                        0,
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_GENERATING_INFO) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        null,
                        0,
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_SAVING_FILE) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_SUCCESS) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.SUCCESS,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_FAIL) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.FAIL,
                        item.getErrorMsg(),
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_CANCEL) {
                partDownloadTask = new HttpPartDownloadTask(item.getSaveDir(),
                        item.getSaveFileName(),
                        System.currentTimeMillis(),
                        Status.CANCELED,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else {
                throw new IllegalArgumentException("illegal state:" + state);
            }
            handlePartDownloadTaskSave(partDownloadTask, item);
            partDownloadTasks.add(partDownloadTask);
        }
        return partDownloadTasks;
    }

    class HttpDownloadTaskFactory implements DownloadTaskFactory {

        @Override
        public DownloadTask createTask(DownloadRequest downloadRequest) {
            if (downloadRequest instanceof HttpDownloadRequest) {
                HttpDownloadRequest httpDownloadRequest = (HttpDownloadRequest) downloadRequest;
                HttpDownloadTask httpDownloadTask = new HttpDownloadTask(downloadRequest.getSaveDir(),
                        downloadRequest.getTargetFileName(),
                        System.currentTimeMillis(),
                        httpDownloadRequest.getUrl(),
                        context,
                        httpDownloadRequest.getThreadSize());

                //保存数据库
                HttpTaskBean httpTaskBean = new HttpTaskBean();
                httpTaskBean.setId(UUID.randomUUID().toString());
                httpTaskBean.setUrl(httpDownloadTask.getUrl());
                httpTaskBean.setThreadSize(httpDownloadTask.getThreadSize());
                httpTaskBean.setSaveDir(httpDownloadTask.getSaveDir());
                httpTaskBean.setTargetFileName(httpDownloadTask.getTargetFileName());
                httpTaskBean.setCreateTime(httpDownloadTask.getCreateTime());
                httpTaskBean.setSaveFileName(httpDownloadTask.getSaveFileName());
                httpTaskBean.setType(HttpTaskBean.TYPE_UNKNOWN);
                httpTaskBean.setState(HttpTaskBean.STATE_IDLE);
                httpTaskService.add(httpTaskBean);

                handleDownloadTaskSave(httpDownloadTask, httpTaskBean);
                return httpDownloadTask;
            }
            return null;
        }
    }

    private void handleDownloadTaskSave(HttpDownloadTask httpDownloadTask, HttpTaskBean httpTaskBean) {
        httpDownloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.IDLE) {
                    httpTaskBean.setState(HttpTaskBean.STATE_IDLE);
                    httpTaskService.update(httpTaskBean);
                } else if (newStatus == Status.DOWNLOADING) {
                    httpTaskBean.setState(HttpTaskBean.STATE_START);
                    httpTaskService.update(httpTaskBean);
                } else if (newStatus == Status.CANCELED) {
                    httpTaskBean.setState(HttpTaskBean.STATE_CANCEL);
                    httpTaskService.update(httpTaskBean);
                } else if (newStatus == Status.SUCCESS) {
                    httpTaskBean.setState(HttpTaskBean.STATE_SUCCESS);
                    httpTaskService.update(httpTaskBean);
                }
            }
        });
        httpDownloadTask.addTaskFailListener(new OnTaskFailListener() {
            @Override
            public void onTaskFail(Throwable e) {
                httpTaskBean.setState(HttpTaskBean.STATE_FAIL);
                httpTaskBean.setErrorMsg(e.getMessage());
                httpTaskService.update(httpTaskBean);
            }
        });
        httpDownloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChange() {
                if (httpTaskBean.getType() == HttpTaskBean.TYPE_DEFAULT) {
                    HttpDefaultTaskBean httpDefaultTaskBean = httpTaskBean.getHttpDefaultTaskBean();
                    httpDefaultTaskBean.setCurrentLength(httpDownloadTask.getCurrentLength());
                    httpTaskService.update(httpTaskBean);
                }
            }
        });
        httpDownloadTask.addOnResourceInfoReadyListener(new OnResourceInfoReadyListener() {
            @Override
            public void onResourceInfoReady(ResourceInfo resourceInfo) {
                httpTaskBean.setContentLength(resourceInfo.getContentLength());
                httpTaskBean.setContentType(resourceInfo.getContentType());
                httpTaskBean.setFileName(resourceInfo.getFileName());
                int responseCode = resourceInfo.getResponseCode();
                httpTaskBean.setResponseCode(responseCode);
                if (responseCode == 200) {
                    httpTaskBean.setType(HttpTaskBean.TYPE_DEFAULT);
                } else if (responseCode == 206) {
                    httpTaskBean.setType(HttpTaskBean.TYPE_PART);
                }
                httpTaskService.update(httpTaskBean);
            }
        });
        httpDownloadTask.addOnSaveFileNameChangeListener(new OnSaveFileNameChangeListener() {
            @Override
            public void onSaveFileNameChange(String name, String oldName) {
                httpTaskBean.setSaveFileName(name);
                httpTaskService.update(httpTaskBean);
            }
        });
        httpDownloadTask.addOnHttpPartDownloadTasksReadyListener(new OnHttpPartDownloadTasksReadyListener() {
            @Override
            public void onHttpPartDownloadTasksReady(List<HttpPartDownloadTask> partDownloadTasks) {
                HttpPartTaskBean httpPartTaskBean = new HttpPartTaskBean();
                List<HttpPartTaskItemBean> partTaskItemBeans = new ArrayList<>();
                for (HttpPartDownloadTask partDownloadTask : partDownloadTasks) {

                    HttpPartTaskItemBean partTaskItemBean = new HttpPartTaskItemBean();
                    partTaskItemBean.setId(UUID.randomUUID().toString());
                    partTaskItemBean.setSaveDir(partDownloadTask.getSaveDir());
                    partTaskItemBean.setSaveFileName(partDownloadTask.getSaveFileName());
                    partTaskItemBean.setCreateTime(System.currentTimeMillis());
                    partTaskItemBean.setStartPosition(partDownloadTask.getStartPosition());
                    partTaskItemBean.setEndPosition(partDownloadTask.getEndPosition());
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_IDLE);
                    httpPartTaskItemService.add(partTaskItemBean);

                    handlePartDownloadTaskSave(partDownloadTask, partTaskItemBean);
                    partTaskItemBeans.add(partTaskItemBean);
                }
                httpPartTaskBean.setItems(partTaskItemBeans);
                httpPartTaskBean.setCreateTime(System.currentTimeMillis());
                httpPartTaskBean.setId(UUID.randomUUID().toString());
                httpPartTaskBean.setState(HttpPartTaskBean.STATE_IDLE);
                httpPartTaskService.add(httpPartTaskBean);

                httpTaskBean.setHttpPartTaskBean(httpPartTaskBean);
                httpTaskService.update(httpTaskBean);
            }
        });
    }

    private void handlePartDownloadTaskSave(HttpPartDownloadTask partDownloadTask, HttpPartTaskItemBean partTaskItemBean) {
        partDownloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.IDLE) {
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_IDLE);
                    httpPartTaskItemService.update(partTaskItemBean);
                } else if (newStatus == Status.DOWNLOADING) {
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_START);
                    httpPartTaskItemService.update(partTaskItemBean);
                } else if (newStatus == Status.SUCCESS) {
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_SUCCESS);
                    httpPartTaskItemService.update(partTaskItemBean);
                } else if (newStatus == Status.CANCELED) {
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_CANCEL);
                    httpPartTaskItemService.update(partTaskItemBean);
                }
            }
        });
        partDownloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChange() {
                partTaskItemBean.setCurrentLength(partDownloadTask.getCurrentLength());
                httpPartTaskItemService.update(partTaskItemBean);
            }
        });
        partDownloadTask.addTaskFailListener(new OnTaskFailListener() {
            @Override
            public void onTaskFail(Throwable e) {
                partTaskItemBean.setState(HttpPartTaskItemBean.STATE_FAIL);
                partTaskItemBean.setErrorMsg(e.getMessage());
                httpPartTaskItemService.update(partTaskItemBean);
            }
        });
    }
}

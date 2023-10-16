package com.zhangqiang.downloadmanager.plugin.http;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.manager.ExecutorManager;
import com.zhangqiang.downloadmanager.manager.OnDownloadTaskDeleteListener;
import com.zhangqiang.downloadmanager.manager.network.NetWorkManager;
import com.zhangqiang.downloadmanager.manager.network.OnAvailableChangedListener;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpDefaultTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpDefaultTaskService;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskItemService;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskService;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpTaskService;
import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.DownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTaskFactory;
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
import com.zhangqiang.downloadmanager.task.interceptor.fail.RetryFailInterceptor;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HttpDownloadPlugin implements DownloadPlugin {

    public static final String TAG = HttpDownloadPlugin.class.getSimpleName();
    private final Context context;
    private final HttpTaskService httpTaskService;
    private final HttpDefaultTaskService httpDefaultTaskService;
    private final HttpPartTaskService httpPartTaskService;
    private final HttpPartTaskItemService httpPartTaskItemService;
    private final HashMap<HttpDownloadTask, HttpTaskBean> mappings = new HashMap<>();

    public HttpDownloadPlugin(Context context) {
        this.context = context;
        this.httpTaskService = new HttpTaskService(context);
        this.httpDefaultTaskService = new HttpDefaultTaskService(context);
        this.httpPartTaskService = new HttpPartTaskService(context);
        this.httpPartTaskItemService = new HttpPartTaskItemService(context);
    }

    @Override
    public void apply(DownloadManager downloadManager) {
        downloadManager.addDownloadTaskFactory(new HttpDownloadTaskFactory());
        downloadManager.addOnDownloadTaskDeleteListener(new OnDownloadTaskDeleteListener() {
            @Override
            public void onDownloadTaskDelete(DownloadTask downloadTask) {
                if (downloadTask instanceof HttpDownloadTask) {
                    HttpDownloadTask httpDownloadTask = (HttpDownloadTask) downloadTask;
                    HttpTaskBean httpTaskBean = mappings.get(httpDownloadTask);
                    if (httpTaskBean == null) {
                        throw new NullPointerException("httpTaskBean are not excepted null");
                    }
                    httpTaskService.remove(httpTaskBean.getId());
                    HttpDefaultTaskBean httpDefaultTaskBean = httpTaskBean.getHttpDefaultTaskBean();
                    if (httpDefaultTaskBean != null) {
                        httpDefaultTaskService.remove(httpDefaultTaskBean.getId());
                    }
                    HttpPartTaskBean httpPartTaskBean = httpTaskBean.getHttpPartTaskBean();
                    if (httpPartTaskBean != null) {
                        httpPartTaskItemService.remove(httpPartTaskBean.getId());

                        List<HttpPartTaskItemBean> items = httpPartTaskBean.getItems();
                        if (items != null) {
                            String dir = null;
                            for (HttpPartTaskItemBean taskItemBean : items) {
                                httpPartTaskItemService.remove(taskItemBean.getId());
                                FileUtils.deleteFile(new File(taskItemBean.getSaveDir(), taskItemBean.getSaveFileName()));
                                dir = taskItemBean.getSaveDir();
                            }
                            if (!TextUtils.isEmpty(dir) && dir != null) {
                                FileUtils.deleteDir(new File(dir));
                            }
                        }
                    }
                    mappings.remove(httpDownloadTask);
                }
            }
        });
        ExecutorManager.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                List<HttpDownloadTask> httpDownloadTasks = loadLocalDownloadTasks();
                downloadManager.addDownloadTasks(httpDownloadTasks);
            }
        });
        NetWorkManager.getInstance(context).addOnAvailableChangedListener(new OnAvailableChangedListener() {
            @Override
            public void onAvailableChanged(boolean available) {
                if (available) {
                    int taskCount = downloadManager.getTaskCount();
                    for (int i = 0; i < taskCount; i++) {
                        DownloadTask task = downloadManager.getTask(i);
                        if (task.getStatus() == Status.FAIL) {
                            LogUtils.i(TAG,"=====start from net available=============");
                            task.start();
                        }
                    }
                }
            }
        });
    }

    @Override
    public void drop() {
    }

    private List<HttpDownloadTask> loadLocalDownloadTasks() {
        List<HttpDownloadTask> httpDownloadTasks = new ArrayList<>();
        List<HttpTaskBean> httpTasks = httpTaskService.getHttpTasks();
        for (int i = 0; i < httpTasks.size(); i++) {
            HttpTaskBean httpTaskBean = httpTasks.get(i);
            int state = httpTaskBean.getState();
            HttpDownloadTask httpDownloadTask;
            if (state == HttpTaskBean.STATE_IDLE) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getId(),
                        httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getPriority(),
                        httpTaskBean.getUrl(),
                        context,
                        httpTaskBean.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl());
            } else if (state == HttpTaskBean.STATE_START
                    || state == HttpTaskBean.STATE_GENERATING_INFO
                    || state == HttpTaskBean.STATE_WAITING_CHILDREN_TASK) {

                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getId(),
                        httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getPriority(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName()
                );
                httpDownloadTask.forceStart();
            } else if (state == HttpTaskBean.STATE_SUCCESS) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getId(),
                        httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getPriority(),
                        Status.SUCCESS,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else if (state == HttpTaskBean.STATE_FAIL) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getId(),
                        httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getPriority(),
                        Status.FAIL,
                        httpTaskBean.getErrorMsg(),
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else if (state == HttpTaskBean.STATE_CANCEL) {
                httpDownloadTask = new HttpDownloadTask(httpTaskBean.getId(),
                        httpTaskBean.getSaveDir(),
                        httpTaskBean.getTargetFileName(),
                        httpTaskBean.getCreateTime(),
                        httpTaskBean.getPriority(),
                        Status.CANCELED,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        getCurrentLength(httpTaskBean),
                        context,
                        httpTaskBean.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl(),
                        makePartDownloadTasks(httpTaskBean, context),
                        httpTaskBean.getSaveFileName());
            } else {
                throw new IllegalArgumentException("unknown http task bean state:" + state);
            }
            handleDownloadTaskCreate(httpDownloadTask, httpTaskBean);
            httpDownloadTasks.add(httpDownloadTask);
        }
        return httpDownloadTasks;
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
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
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
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_GENERATING_INFO) {
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
                        Status.DOWNLOADING,
                        null,
                        httpTaskBean.getUrl(),
                        makeResourceInfo(httpTaskBean),
                        item.getCurrentLength(),
                        context,
                        item.getStartPosition(),
                        item.getEndPosition()
                );
            } else if (state == HttpPartTaskItemBean.STATE_SAVING_FILE) {
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
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
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
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
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
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
                partDownloadTask = new HttpPartDownloadTask(item.getId(),
                        item.getSaveDir(),
                        item.getSaveFileName(),
                        item.getCreateTime(),
                        item.getPriority(),
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

    private int getDefaultPriority() {
        return 0;
    }

    class HttpDownloadTaskFactory implements DownloadTaskFactory {

        @Override
        public DownloadTask createTask(DownloadRequest downloadRequest) {
            if (downloadRequest instanceof HttpDownloadRequest) {

                HttpDownloadRequest httpDownloadRequest = (HttpDownloadRequest) downloadRequest;
                HttpDownloadTask httpDownloadTask = new HttpDownloadTask(generateId(),
                        downloadRequest.getSaveDir(),
                        downloadRequest.getTargetFileName(),
                        System.currentTimeMillis(),
                        getDefaultPriority(),
                        httpDownloadRequest.getUrl(),
                        context,
                        httpDownloadRequest.getThreadSize(),
                        new HttpPartDownloadTaskFactoryImpl());

                //保存数据库
                HttpTaskBean httpTaskBean = new HttpTaskBean();
                httpTaskBean.setId(httpDownloadTask.getId());
                httpTaskBean.setUrl(httpDownloadTask.getUrl());
                httpTaskBean.setThreadSize(httpDownloadTask.getThreadSize());
                httpTaskBean.setSaveDir(httpDownloadTask.getSaveDir());
                httpTaskBean.setTargetFileName(httpDownloadTask.getTargetFileName());
                httpTaskBean.setCreateTime(httpDownloadTask.getCreateTime());
                httpTaskBean.setSaveFileName(httpDownloadTask.getSaveFileName());
                httpTaskBean.setType(HttpTaskBean.TYPE_UNKNOWN);
                httpTaskBean.setState(HttpTaskBean.STATE_IDLE);
                httpTaskService.add(httpTaskBean);

                handleDownloadTaskCreate(httpDownloadTask, httpTaskBean);
                return httpDownloadTask;
            }
            return null;
        }
    }

    private void handleDownloadTaskCreate(HttpDownloadTask httpDownloadTask, HttpTaskBean httpTaskBean) {
        handleDownloadTaskSave(httpDownloadTask, httpTaskBean);
        httpDownloadTask.addFailInterceptor(new RetryFailInterceptor(httpDownloadTask));
    }

    private void handleDownloadTaskSave(HttpDownloadTask httpDownloadTask, HttpTaskBean httpTaskBean) {
        mappings.put(httpDownloadTask, httpTaskBean);
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
                    httpDefaultTaskService.update(httpDefaultTaskBean);
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

                    HttpDefaultTaskBean httpDefaultTaskBean = new HttpDefaultTaskBean();
                    httpDefaultTaskBean.setId(generateId());
                    httpDefaultTaskBean.setCreateTime(System.currentTimeMillis());
                    httpDefaultTaskBean.setCurrentLength(0);
                    httpDefaultTaskService.add(httpDefaultTaskBean);

                    httpTaskBean.setType(HttpTaskBean.TYPE_DEFAULT);
                    httpTaskBean.setHttpDefaultTaskBean(httpDefaultTaskBean);
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
                    partTaskItemBean.setId(partDownloadTask.getId());
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
                httpPartTaskBean.setId(generateId());
                httpPartTaskBean.setCreateTime(System.currentTimeMillis());
                httpPartTaskBean.setItems(partTaskItemBeans);
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

    private class HttpPartDownloadTaskFactoryImpl implements HttpPartDownloadTaskFactory {

        @Override
        public HttpPartDownloadTask createHttpPartDownloadTask(String saveDir,
                                                               String targetFileName,
                                                               String url,
                                                               long startPosition,
                                                               long endPosition) {

            return new HttpPartDownloadTask(generateId(),
                    saveDir,
                    targetFileName,
                    System.currentTimeMillis(),
                    getDefaultPriority(),
                    url,
                    context,
                    startPosition,
                    endPosition);
        }
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}

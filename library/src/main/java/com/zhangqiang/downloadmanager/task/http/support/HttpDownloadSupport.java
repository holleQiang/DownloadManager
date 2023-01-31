package com.zhangqiang.downloadmanager.task.http.support;

import android.content.Context;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.http.part.PartInfo;
import com.zhangqiang.downloadmanager.task.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.task.http.service.HttpDefaultTaskService;
import com.zhangqiang.downloadmanager.task.http.service.HttpPartTaskItemService;
import com.zhangqiang.downloadmanager.task.http.service.HttpPartTaskService;
import com.zhangqiang.downloadmanager.task.http.service.HttpTaskService;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.bean.HttpDefaultTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpTaskBean;
import com.zhangqiang.downloadmanager.task.http.callback.Callback;
import com.zhangqiang.downloadmanager.task.http.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.task.http.engine.HttpEngine;
import com.zhangqiang.downloadmanager.task.http.engine.okhttp.OkHttpEngine;
import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.part.HttpPartTaskFactory;
import com.zhangqiang.downloadmanager.task.speed.SpeedUtils;
import com.zhangqiang.downloadmanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class HttpDownloadSupport implements DownloadSupport {

    public static final String TAG = "HttpDownloadSupport";

    private final HttpEngine mHttpEngine;
    private final HttpTaskService mHttpTaskService;
    private final HttpDefaultTaskService mHttpDefaultTaskService;
    private final HttpPartTaskService mHttpPartTaskService;
    private final HttpPartTaskItemService mHttpPartTaskItemService;

    public HttpDownloadSupport(Context context) {
        mHttpEngine = new OkHttpEngine(context);
        mHttpTaskService = new HttpTaskService(context);
        mHttpDefaultTaskService = new HttpDefaultTaskService(context);
        mHttpPartTaskService = new HttpPartTaskService(context);
        mHttpPartTaskItemService = new HttpPartTaskItemService(context);
    }

    @Override
    public List<DownloadTask> loadDownloadTasks() {
        List<HttpTaskBean> httpTaskBeans = mHttpTaskService.getHttpTasks();
        if (httpTaskBeans == null || httpTaskBeans.isEmpty()) {
            return null;
        }
        List<DownloadTask> downloadTasks = new ArrayList<>();
        for (HttpTaskBean httpTaskBean : httpTaskBeans) {
            InternalTask httpDownloadTask = new InternalTask(httpTaskBean.getId(),
                    mHttpEngine,
                    httpTaskBean.getUrl(),
                    httpTaskBean.getSaveDir(),
                    httpTaskBean.getFileName(),
                    httpTaskBean.getThreadSize(),
                    new HttpPartTaskFactoryImpl(httpTaskBean));
            configHttpDownloadTask(httpTaskBean, httpDownloadTask);
            downloadTasks.add(httpDownloadTask);
        }
        return downloadTasks;
    }

    @Override
    public DownloadTask createDownloadTask(DownloadRequest request) {
        if (!(request instanceof HttpDownloadRequest)) {
            return null;
        }
        HttpDownloadRequest httpDownloadRequest = (HttpDownloadRequest) request;
        HttpTaskBean httpTaskBean = new HttpTaskBean();
        String taskId = UUID.randomUUID().toString();
        httpTaskBean.setId(taskId);
        httpTaskBean.setSaveDir(request.getSaveDir());
        httpTaskBean.setThreadSize(httpDownloadRequest.getThreadSize());
        httpTaskBean.setUrl(httpDownloadRequest.getUrl());
        httpTaskBean.setState(HttpTaskBean.STATE_IDLE);
        httpTaskBean.setType(HttpTaskBean.TYPE_UNKNOWN);
        httpTaskBean.setFileName(request.getFileName());
        httpTaskBean.setTargetFileName(request.getFileName());
        httpTaskBean.setCreateTime(new Date());

        mHttpTaskService.add(httpTaskBean);

        InternalTask httpDownloadTask = new InternalTask(taskId,
                mHttpEngine,
                httpTaskBean.getUrl(),
                httpTaskBean.getSaveDir(),
                httpTaskBean.getFileName(),
                httpTaskBean.getThreadSize(),
                new HttpPartTaskFactoryImpl(httpTaskBean)
        );
        configHttpDownloadTask(httpTaskBean, httpDownloadTask);
        return httpDownloadTask;
    }

    @Override
    public TaskInfo buildTaskInfo(DownloadTask downloadTask) {
        return new TaskInfoImpl(((InternalTask) downloadTask).httpTaskBean, downloadTask);
    }


    @Override
    public boolean handleProgressSync(DownloadTask downloadTask) {
        InternalTask internalTask = (InternalTask) downloadTask;
        HttpTaskBean httpTaskBean = internalTask.httpTaskBean;
        int httpTaskType = httpTaskBean.getType();
        HttpDownloadTask httpDownloadTask = (HttpDownloadTask) downloadTask;
        if (httpTaskType == HttpTaskBean.TYPE_DEFAULT) {
            HttpDefaultTaskBean httpDefaultTask = httpTaskBean.getHttpDefaultTask();
            long oldLength = httpDefaultTask.getCurrentLength();
            long newLength = httpDownloadTask.getCurrentLength();
            if (oldLength != newLength) {
                httpDefaultTask.setCurrentLength(newLength);
                mHttpDefaultTaskService.update(httpDefaultTask);
                return true;
            }
        } else if (httpTaskType == HttpTaskBean.TYPE_PART) {
            List<? extends DownloadTask> partRecords = internalTask.getPartTasks();
            if (partRecords == null) {
                return false;
            }
            boolean changed = false;
            for (DownloadTask httpDownloadPartTask : partRecords) {
                long newLength = httpDownloadPartTask.getCurrentLength();
                HttpPartTaskItemBean httpPartTaskItemBean = ((InternalPartTask) httpDownloadPartTask).httpPartTaskItemBean;
                long oldLength = httpPartTaskItemBean.getCurrentPosition() - httpPartTaskItemBean.getStartPosition();
                if (newLength != oldLength) {
                    httpPartTaskItemBean.setCurrentPosition(httpDownloadPartTask.getCurrentLength() + httpPartTaskItemBean.getStartPosition());
                    mHttpPartTaskItemService.update(httpPartTaskItemBean);
                    changed = true;
                }
                return changed;
            }
        }
        return false;
    }

    @Override
    public boolean handleSpeedCompute(DownloadTask downloadTask) {
        if (SpeedUtils.computeSpeed(downloadTask)) {

            List<HttpDownloadPartTask> partTasks = ((HttpDownloadTask) downloadTask).getPartTasks();
            if (partTasks != null && !partTasks.isEmpty()) {
                for (DownloadTask childTask : partTasks) {
                    SpeedUtils.computeSpeed(childTask);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isTaskIdle(DownloadTask downloadTask) {
        HttpTaskBean httpTaskBean = ((InternalTask) downloadTask).httpTaskBean;
        return httpTaskBean.getState() == HttpTaskBean.STATE_IDLE;
    }

    @Override
    public boolean isTaskRunning(DownloadTask downloadTask) {
        HttpTaskBean httpTaskBean = ((InternalTask) downloadTask).httpTaskBean;
        int state = httpTaskBean.getState();
        return state == HttpTaskBean.STATE_START
                || state == HttpTaskBean.STATE_GENERATING_INFO
                || state == HttpTaskBean.STATE_WAITING_CHILDREN_TASK;
    }

    @Override
    public void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile) {
        HttpTaskBean httpTaskBean = ((InternalTask) downloadTask).httpTaskBean;
        mHttpTaskService.remove(httpTaskBean.getId());
        try {
            FileUtils.deleteFileIfExists(new File(httpTaskBean.getSaveDir(), httpTaskBean.getFileName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int type = httpTaskBean.getType();
        if (type == HttpTaskBean.TYPE_DEFAULT) {
            HttpDefaultTaskBean httpDefaultTask = httpTaskBean.getHttpDefaultTask();
            if (httpDefaultTask != null) {
                mHttpDefaultTaskService.remove(httpDefaultTask.getId());
            }
        } else if (type == HttpTaskBean.TYPE_PART) {
            HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
            mHttpPartTaskService.remove(httpPartTask.getId());
            List<HttpPartTaskItemBean> items = httpPartTask.getItems();
            if (items != null) {
                for (HttpPartTaskItemBean item : items) {
                    mHttpPartTaskItemService.remove(item.getId());
                    try {
                        FileUtils.deleteFileIfExists(new File(item.getFilePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void configHttpDownloadTask(HttpTaskBean httpTaskBean, InternalTask httpDownloadTask) {
        httpDownloadTask.httpTaskBean = httpTaskBean;
        httpDownloadTask.getCallbacks().addCallback(new Callback() {

            @Override
            public void onStartGenerateInfo() {
                httpTaskBean.setState(HttpTaskBean.STATE_GENERATING_INFO);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onResourceInfoReady(ResourceInfo info) {
                httpTaskBean.setFileName(info.getFileName());
                httpTaskBean.setContentType(info.getContentType());
                httpTaskBean.setContentLength(info.getContentLength());
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onStartDefaultDownload() {
                httpTaskBean.setType(HttpTaskBean.TYPE_DEFAULT);
                httpTaskBean.setHttpPartTask(null);
                HttpDefaultTaskBean httpDefaultTaskBean = httpTaskBean.getHttpDefaultTask();
                if (httpDefaultTaskBean == null) {
                    httpDefaultTaskBean = new HttpDefaultTaskBean();
                    httpDefaultTaskBean.setId(UUID.randomUUID().toString());
                    httpDefaultTaskBean.setState(HttpDefaultTaskBean.STATE_IDLE);
                    httpDefaultTaskBean.setCurrentLength(0);
                    httpDefaultTaskBean.setCreateTime(new Date());
                    mHttpDefaultTaskService.add(httpDefaultTaskBean);
                    httpTaskBean.setHttpDefaultTask(httpDefaultTaskBean);
                }
                httpTaskBean.setState(HttpTaskBean.STATE_WAITING_CHILDREN_TASK);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onStartPartDownload() {
                httpTaskBean.setType(HttpTaskBean.TYPE_PART);
                httpTaskBean.setHttpDefaultTask(null);
                HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
                if (httpPartTask == null) {
                    httpPartTask = new HttpPartTaskBean();
                    httpPartTask.setId(UUID.randomUUID().toString());
                    httpPartTask.setState(HttpPartTaskBean.STATE_IDLE);
                    httpPartTask.setCreateTime(new Date());
                    mHttpPartTaskService.add(httpPartTask);
                    httpTaskBean.setHttpPartTask(httpPartTask);
                }
                httpTaskBean.setState(HttpTaskBean.STATE_WAITING_CHILDREN_TASK);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onPartTaskFail(HttpDownloadPartTask task, Throwable e) {
                HttpPartTaskItemBean httpPartTaskItemBean = ((InternalPartTask) task).httpPartTaskItemBean;
                httpPartTaskItemBean.setState(HttpPartTaskItemBean.STATE_FAIL);
                httpPartTaskItemBean.setErrorMsg(e.getMessage());
                mHttpPartTaskItemService.update(httpPartTaskItemBean);
            }
        });
        httpDownloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onReset() {
                httpTaskBean.setState(HttpTaskBean.STATE_IDLE);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onStart() {
                httpTaskBean.setState(HttpTaskBean.STATE_START);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onComplete() {
                httpTaskBean.setState(HttpTaskBean.STATE_SUCCESS);
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onFail(DownloadException e) {
                httpTaskBean.setState(HttpTaskBean.STATE_FAIL);
                httpTaskBean.setErrorMsg(e.getMessage());
                mHttpTaskService.update(httpTaskBean);
            }

            @Override
            public void onCancel() {
                httpTaskBean.setState(HttpTaskBean.STATE_CANCEL);
                mHttpTaskService.update(httpTaskBean);
            }
        });
    }

    private static class InternalTask extends HttpDownloadTask {

        private HttpTaskBean httpTaskBean;

        public InternalTask(String id,
                            HttpEngine httpEngine,
                            String url,
                            String saveDir,
                            String targetFileName,
                            int threadSize,
                            HttpPartTaskFactory httpPartTaskFactory) {
            super(id, httpEngine, url, saveDir, targetFileName, threadSize, httpPartTaskFactory);
        }
    }

    private static class InternalPartTask extends HttpDownloadPartTask {

        private HttpPartTaskItemBean httpPartTaskItemBean;

        public InternalPartTask(String id,
                                HttpEngine httpEngine,
                                String url,
                                long fromPosition,
                                long currentPosition,
                                long toPosition,
                                String filePath) {
            super(id, httpEngine, url, fromPosition, currentPosition, toPosition, filePath);
        }
    }

    private class HttpPartTaskFactoryImpl implements HttpPartTaskFactory {

        private final HttpTaskBean httpTaskBean;

        public HttpPartTaskFactoryImpl(HttpTaskBean httpTaskBean) {
            this.httpTaskBean = httpTaskBean;
        }

        @Override
        public List<HttpDownloadPartTask> onCreateHttpPartTask(String url, PartInfo partInfo) {
            ArrayList<HttpDownloadPartTask> partTasks = new ArrayList<>();
            List<PartInfo.PartFile> partFiles = partInfo.getPartFiles();
            HttpPartTaskBean httpPartTaskBean = httpTaskBean.getHttpPartTask();
            List<HttpPartTaskItemBean> items = httpPartTaskBean.getItems();
            if (items != null && items.size() == partFiles.size()) {
                // recover from record
                for (PartInfo.PartFile partFile : partFiles) {
                    long start = partFile.getStart();
                    long end = partFile.getEnd();
                    String filePath = partFile.getFile().getAbsolutePath();

                    HttpPartTaskItemBean historyPartItemBean = null;
                    for (HttpPartTaskItemBean httpPartTaskItemBean : items) {
                        if (httpPartTaskItemBean.getFilePath().equals(filePath)
                                && httpPartTaskItemBean.getStartPosition() == start
                                && httpPartTaskItemBean.getEndPosition() == end) {
                            historyPartItemBean = httpPartTaskItemBean;
                        }
                    }
                    if (historyPartItemBean != null) {
                        InternalPartTask internalPartTask = new InternalPartTask(historyPartItemBean.getId(),
                                mHttpEngine,
                                url,
                                historyPartItemBean.getStartPosition(),
                                historyPartItemBean.getCurrentPosition(),
                                historyPartItemBean.getEndPosition(),
                                historyPartItemBean.getFilePath());
                        internalPartTask.httpPartTaskItemBean = historyPartItemBean;
                        partTasks.add(internalPartTask);
                    } else {
                        break;
                    }
                }
                if (partTasks.size() == partFiles.size()) {
                    return partTasks;
                } else {
                    //recover fail ,remove invalid record
                    httpPartTaskBean.setItems(null);
                    mHttpPartTaskService.update(httpPartTaskBean);
                    mHttpPartTaskItemService.remove(items);
                }
            }
            //create new part task
            items = new ArrayList<>();
            for (PartInfo.PartFile partFile : partFiles) {
                long start = partFile.getStart();
                long end = partFile.getEnd();
                String filePath = partFile.getFile().getAbsolutePath();

                HttpPartTaskItemBean httpPartTaskItemBean = new HttpPartTaskItemBean();
                httpPartTaskItemBean.setId(UUID.randomUUID().toString());
                httpPartTaskItemBean.setCreateTime(new Date());
                httpPartTaskItemBean.setState(HttpPartTaskItemBean.STATE_IDLE);
                httpPartTaskItemBean.setFilePath(filePath);
                httpPartTaskItemBean.setStartPosition(start);
                httpPartTaskItemBean.setCurrentPosition(start);
                httpPartTaskItemBean.setEndPosition(end);
                items.add(httpPartTaskItemBean);

                InternalPartTask partTask = new InternalPartTask(httpPartTaskItemBean.getId(),
                        mHttpEngine,
                        url,
                        start,
                        start,
                        end,
                        filePath);
                partTask.httpPartTaskItemBean = httpPartTaskItemBean;
                partTasks.add(partTask);
            }
            httpPartTaskBean.setItems(items);
            mHttpPartTaskService.update(httpPartTaskBean);
            mHttpPartTaskItemService.add(items);
            return partTasks;
        }
    }
}

package com.zhangqiang.downloadmanager.plugin.m3u8;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.ExecutorManager;
import com.zhangqiang.downloadmanager.manager.OnDownloadTaskDeleteListener;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpTaskBean;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskItemService;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.plugin.http.task.ResourceInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.bean.M3u8TaskBean;
import com.zhangqiang.downloadmanager.plugin.m3u8.bean.TSTaskBean;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.request.M3u8DownloadRequest;
import com.zhangqiang.downloadmanager.plugin.m3u8.service.M3u8TaskService;
import com.zhangqiang.downloadmanager.plugin.m3u8.service.TSTaskService;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.M3u8DownloadTask;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.M3u8ResourceInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.OnTSDownloadBundlesReadyListener;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.TSDownloadBundle;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.TSDownloadBundleFactory;
import com.zhangqiang.downloadmanager.plugin.m3u8.utils.Utils;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.OnTaskFailListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class M3u8DownloadPlugin extends SimpleDownloadPlugin {

    private final Context context;
    private final M3u8TaskService m3u8TaskService;
    private final TSTaskService tsTaskService;
    private final HttpPartTaskItemService httpPartTaskItemService;
    private final HashMap<M3u8DownloadTask, M3u8TaskBean> mappings = new HashMap<>();

    public M3u8DownloadPlugin(Context context) {
        this.context = context.getApplicationContext();
        m3u8TaskService = new M3u8TaskService(this.context);
        tsTaskService = new TSTaskService(this.context);
        httpPartTaskItemService = new HttpPartTaskItemService(this.context);
    }

    @Override
    public String getName() {
        return "m3u8视频下载插件";
    }

    @Override
    protected void onApply(DownloadManager downloadManager) {
        super.onApply(downloadManager);
        downloadManager.addDownloadTaskFactory(new DownloadTaskFactory() {
            @Override
            public DownloadTask createTask(DownloadRequest request) {
                if (request instanceof M3u8DownloadRequest) {
                    M3u8DownloadRequest m3u8DownloadRequest = (M3u8DownloadRequest) request;
                    M3u8DownloadTask m3u8DownloadTask = new M3u8DownloadTask(generateId(),
                            request.getSaveDir(),
                            request.getTargetFileName(),
                            System.currentTimeMillis(),
                            context,
                            m3u8DownloadRequest.getUrl(),
                            new TSDownloadBundleFactoryImpl());
                    handleDownloadTaskCreate(m3u8DownloadTask);
                    return m3u8DownloadTask;
                }
                return null;
            }
        });
        downloadManager.addOnDownloadTaskDeleteListener(new OnDownloadTaskDeleteListener() {
            @Override
            public void onDownloadTaskDelete(DownloadTask downloadTask) {
                if (downloadTask instanceof M3u8DownloadTask) {
                    M3u8DownloadTask m3u8DownloadTask = (M3u8DownloadTask) downloadTask;
                    M3u8TaskBean taskBean = mappings.get(m3u8DownloadTask);
                    if (taskBean == null) {
                        throw new NullPointerException("taskBean are not excepted null");
                    }
                    m3u8TaskService.remove(taskBean.getId());
                    List<TSTaskBean> tsTaskBeans = taskBean.getTsTaskBeans();
                    if (tsTaskBeans != null) {

                        String dir = null;
                        List<String> ids = new ArrayList<>();
                        List<String> childIds = new ArrayList<>();
                        for (TSTaskBean tsTaskBean : tsTaskBeans) {
                            ids.add(tsTaskBean.getId());
                            HttpPartTaskItemBean taskItemBean = tsTaskBean.getHttpPartTaskItemBean();
                            if (taskItemBean != null) {
                                childIds.add(taskItemBean.getId());
                                dir = taskItemBean.getSaveDir();
                                FileUtils.deleteFile(new File(taskItemBean.getSaveDir(), taskItemBean.getSaveFileName()));
                            }
                        }
                        if (!TextUtils.isEmpty(dir) && dir != null) {
                            FileUtils.deleteDir(new File(dir));
                        }
                        tsTaskService.remove(ids);
                        httpPartTaskItemService.removeByIds(childIds);
                    }
                    mappings.remove(m3u8DownloadTask);
                }
            }
        });
        ExecutorManager.getInstance().submit(new Runnable() {
            @Override
            public void run() {
                List<M3u8DownloadTask> downloadTasks = loadLocalDownloadTasks();
                downloadManager.addDownloadTasks(downloadTasks);
            }
        });
    }

    private List<M3u8DownloadTask> loadLocalDownloadTasks() {
        List<M3u8DownloadTask> downloadTasks = new ArrayList<>();
        List<M3u8TaskBean> tasks = m3u8TaskService.getM3u8Tasks();
        for (int i = 0; i < tasks.size(); i++) {
            M3u8TaskBean taskBean = tasks.get(i);
            int state = taskBean.getState();
            M3u8DownloadTask downloadTask;
            if (state == M3u8TaskBean.STATE_IDLE) {
                downloadTask = new M3u8DownloadTask(taskBean.getId(),
                        taskBean.getSaveDir(),
                        taskBean.getTargetFileName(),
                        taskBean.getCreateTime(),
                        context,
                        taskBean.getUrl(),
                        new TSDownloadBundleFactoryImpl());
            } else if (state == M3u8TaskBean.STATE_START
                    || state == M3u8TaskBean.STATE_GENERATING_INFO
                    || state == M3u8TaskBean.STATE_WAITING_CHILDREN_TASK) {

                downloadTask = new M3u8DownloadTask(taskBean.getId(),
                        taskBean.getSaveDir(),
                        taskBean.getTargetFileName(),
                        taskBean.getCreateTime(),
                        Status.DOWNLOADING,
                        null,
                        getCurrentLength(taskBean),
                        context,
                        taskBean.getUrl(),
                        new TSDownloadBundleFactoryImpl(),
                        makeTSDownloadBundles(taskBean),
                        makeResourceInfo(taskBean),
                        getCurrentDuration(taskBean)
                );
            } else if (state == M3u8TaskBean.STATE_SUCCESS) {
                downloadTask = new M3u8DownloadTask(taskBean.getId(),
                        taskBean.getSaveDir(),
                        taskBean.getTargetFileName(),
                        taskBean.getCreateTime(),
                        Status.SUCCESS,
                        null,
                        getCurrentLength(taskBean),
                        context,
                        taskBean.getUrl(),
                        new TSDownloadBundleFactoryImpl(),
                        makeTSDownloadBundles(taskBean),
                        makeResourceInfo(taskBean),
                        getCurrentDuration(taskBean));
            } else if (state == M3u8TaskBean.STATE_FAIL) {
                downloadTask = new M3u8DownloadTask(taskBean.getId(),
                        taskBean.getSaveDir(),
                        taskBean.getTargetFileName(),
                        taskBean.getCreateTime(),
                        Status.FAIL,
                        taskBean.getErrorMsg(),
                        getCurrentLength(taskBean),
                        context,
                        taskBean.getUrl(),
                        new TSDownloadBundleFactoryImpl(),
                        makeTSDownloadBundles(taskBean),
                        makeResourceInfo(taskBean),
                        getCurrentDuration(taskBean));
            } else if (state == M3u8TaskBean.STATE_CANCEL) {
                downloadTask = new M3u8DownloadTask(taskBean.getId(),
                        taskBean.getSaveDir(),
                        taskBean.getTargetFileName(),
                        taskBean.getCreateTime(),
                        Status.CANCELED,
                        null,
                        getCurrentLength(taskBean),
                        context,
                        taskBean.getUrl(),
                        new TSDownloadBundleFactoryImpl(),
                        makeTSDownloadBundles(taskBean),
                        makeResourceInfo(taskBean),
                        getCurrentDuration(taskBean));
            } else {
                throw new IllegalArgumentException("unknown http task bean state:" + state);
            }
            handleDownloadTaskSave(downloadTask, taskBean);
            downloadTasks.add(downloadTask);
        }
        return downloadTasks;
    }

    private float getCurrentDuration(M3u8TaskBean taskBean) {
        float duration = 0;
        List<TSTaskBean> tsTaskBeans = taskBean.getTsTaskBeans();
        if (tsTaskBeans != null) {
            for (TSTaskBean tsTaskBean : tsTaskBeans) {
                HttpPartTaskItemBean httpPartTaskItemBean = tsTaskBean.getHttpPartTaskItemBean();
                if (httpPartTaskItemBean != null && httpPartTaskItemBean.getState() == HttpPartTaskItemBean.STATE_SUCCESS) {
                    duration += tsTaskBean.getDuration();
                }
            }
        }
        return duration;
    }

    private List<TSDownloadBundle> makeTSDownloadBundles(M3u8TaskBean taskBean) {
        List<TSTaskBean> tsTaskBeans = taskBean.getTsTaskBeans();
        if (tsTaskBeans != null) {
            List<TSDownloadBundle> bundles = new ArrayList<>();
            for (TSTaskBean tsTaskBean : tsTaskBeans) {
                HttpPartTaskItemBean partTaskItemBean = tsTaskBean.getHttpPartTaskItemBean();
                int state = partTaskItemBean.getState();
                HttpPartDownloadTask partDownloadTask;
                if (state == HttpPartTaskItemBean.STATE_IDLE) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.IDLE,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_START) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.DOWNLOADING,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_GENERATING_INFO) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.DOWNLOADING,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_SAVING_FILE) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.DOWNLOADING,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_SUCCESS) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.SUCCESS,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_FAIL) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.FAIL,
                            partTaskItemBean.getErrorMsg(),
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else if (state == HttpPartTaskItemBean.STATE_CANCEL) {
                    partDownloadTask = new HttpPartDownloadTask(partTaskItemBean.getId(),
                            partTaskItemBean.getSaveDir(),
                            partTaskItemBean.getSaveFileName(),
                            partTaskItemBean.getCreateTime(),
                            Status.CANCELED,
                            null,
                            Utils.buildResourceUrl(taskBean.getUrl(), tsTaskBean.getUri()),
                            makePartResourceInfo(partTaskItemBean),
                            partTaskItemBean.getCurrentLength(),
                            context,
                            partTaskItemBean.getStartPosition(),
                            partTaskItemBean.getEndPosition()
                    );
                } else {
                    throw new IllegalArgumentException("illegal state:" + state);
                }
                handlePartDownloadTaskSave(partDownloadTask, partTaskItemBean);
                bundles.add(new TSDownloadBundle(new TSInfo(tsTaskBean.getDuration(), tsTaskBean.getUri()), partDownloadTask));
            }
            return bundles;
        }
        return null;
    }

    private ResourceInfo makePartResourceInfo(HttpPartTaskItemBean taskBean) {
        return new ResourceInfo(null, taskBean.getEndPosition() - taskBean.getStartPosition(), null, -1);
    }

    private long getCurrentLength(M3u8TaskBean taskBean) {
        long length = 0;
        List<TSTaskBean> tsTaskBeans = taskBean.getTsTaskBeans();
        if (tsTaskBeans != null) {
            for (TSTaskBean tsTaskBean : tsTaskBeans) {
                HttpPartTaskItemBean itemBean = tsTaskBean.getHttpPartTaskItemBean();
                if (itemBean != null) {
                    length += itemBean.getCurrentLength();
                }
            }
        }
        return length;
    }

    private M3u8ResourceInfo makeResourceInfo(M3u8TaskBean taskBean) {
        return new M3u8ResourceInfo(taskBean.getDuration(), taskBean.getM3u8FileInfo());
    }

    private class TSDownloadBundleFactoryImpl implements TSDownloadBundleFactory {

        @Override
        public TSDownloadBundle createTSDownloadBundle(String saveDir, String targetFileName, String url, long startPosition, long endPosition, TSInfo info) {
            return new TSDownloadBundle(info, new HttpPartDownloadTask(
                    generateId(),
                    saveDir,
                    targetFileName,
                    System.currentTimeMillis(),
                    url,
                    context,
                    startPosition,
                    endPosition));
        }
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

    private void handleDownloadTaskCreate(M3u8DownloadTask downloadTask) {
        //保存数据库
        M3u8TaskBean m3u8TaskBean = new M3u8TaskBean();
        m3u8TaskBean.setId(downloadTask.getId());
        m3u8TaskBean.setUrl(downloadTask.getUrl());
        m3u8TaskBean.setSaveDir(downloadTask.getSaveDir());
        m3u8TaskBean.setTargetFileName(downloadTask.getTargetFileName());
        m3u8TaskBean.setCreateTime(downloadTask.getCreateTime());
        m3u8TaskBean.setSaveFileName(downloadTask.getSaveFileName());
        m3u8TaskBean.setState(HttpTaskBean.STATE_IDLE);
        m3u8TaskService.add(m3u8TaskBean);
        handleDownloadTaskSave(downloadTask, m3u8TaskBean);
    }

    private void handleDownloadTaskSave(M3u8DownloadTask downloadTask, M3u8TaskBean taskBean) {
        mappings.put(downloadTask, taskBean);
        downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.IDLE) {
                    taskBean.setState(HttpTaskBean.STATE_IDLE);
                    m3u8TaskService.update(taskBean);
                } else if (newStatus == Status.DOWNLOADING) {
                    taskBean.setState(HttpTaskBean.STATE_START);
                    m3u8TaskService.update(taskBean);
                } else if (newStatus == Status.CANCELED) {
                    taskBean.setState(HttpTaskBean.STATE_CANCEL);
                    m3u8TaskService.update(taskBean);
                } else if (newStatus == Status.SUCCESS) {
                    taskBean.setState(HttpTaskBean.STATE_SUCCESS);
                    m3u8TaskService.update(taskBean);
                }
            }
        });
        downloadTask.addTaskFailListener(new OnTaskFailListener() {
            @Override
            public void onTaskFail(Throwable e) {
                taskBean.setState(HttpTaskBean.STATE_FAIL);
                taskBean.setErrorMsg(e.getMessage());
                m3u8TaskService.update(taskBean);
            }
        });
        downloadTask.addOnResourceInfoReadyListener(new OnResourceInfoReadyListener() {

            @Override
            public void onResourceInfoReady(M3u8ResourceInfo resourceInfo) {
                taskBean.setDuration(resourceInfo.getDuration());
                taskBean.setM3u8FileInfo(resourceInfo.getM3u8File());
                m3u8TaskService.update(taskBean);
            }
        });
        downloadTask.addOnTSDownloadBundlesReadyListener(new OnTSDownloadBundlesReadyListener() {
            @Override
            public void onTSBundlesReady(List<TSDownloadBundle> bundles) {
                List<TSTaskBean> tsTaskBeans = new ArrayList<>();
                List<HttpPartTaskItemBean> partTaskItemBeans = new ArrayList<>();
                for (TSDownloadBundle bundle : bundles) {

                    TSInfo info = bundle.getInfo();
                    HttpPartDownloadTask downloadTask = bundle.getDownloadTask();

                    TSTaskBean tsTaskBean = new TSTaskBean();
                    tsTaskBean.setId(generateId());
                    tsTaskBean.setDuration(info.getDuration());
                    tsTaskBean.setUri(info.getUri());

                    HttpPartTaskItemBean partTaskItemBean = new HttpPartTaskItemBean();
                    partTaskItemBean.setId(downloadTask.getId());
                    partTaskItemBean.setSaveDir(downloadTask.getSaveDir());
                    partTaskItemBean.setSaveFileName(downloadTask.getSaveFileName());
                    partTaskItemBean.setCreateTime(System.currentTimeMillis());
                    partTaskItemBean.setStartPosition(downloadTask.getStartPosition());
                    partTaskItemBean.setEndPosition(downloadTask.getEndPosition());
                    partTaskItemBean.setState(HttpPartTaskItemBean.STATE_IDLE);
                    handlePartDownloadTaskSave(downloadTask, partTaskItemBean);
                    partTaskItemBeans.add(partTaskItemBean);

                    tsTaskBean.setHttpPartTaskItemBean(partTaskItemBean);
                    tsTaskBeans.add(tsTaskBean);
                }
                tsTaskService.add(tsTaskBeans);
                httpPartTaskItemService.add(partTaskItemBeans);

                taskBean.setTsTaskBeans(tsTaskBeans);
                m3u8TaskService.update(taskBean);
            }
        });
    }

    private static String generateId() {
        return UUID.randomUUID().toString();
    }
}

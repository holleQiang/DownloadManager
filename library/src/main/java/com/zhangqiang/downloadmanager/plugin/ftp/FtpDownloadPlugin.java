package com.zhangqiang.downloadmanager.plugin.ftp;

import android.content.Context;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.DownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.ftp.bean.FTPTaskBean;
import com.zhangqiang.downloadmanager.plugin.ftp.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.plugin.ftp.request.FtpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.ftp.service.FTPTaskService;
import com.zhangqiang.downloadmanager.plugin.ftp.task.FTPDownloadTask;
import com.zhangqiang.downloadmanager.plugin.ftp.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTaskFactory;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.OnTaskFailListener;
import com.zhangqiang.downloadmanager.task.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FtpDownloadPlugin implements DownloadPlugin {

    private final FTPTaskService ftpTaskService;

    public FtpDownloadPlugin(Context context) {
        ftpTaskService = new FTPTaskService(context);
    }

    @Override
    public void apply(DownloadManager downloadManager) {
        downloadManager.addDownloadTaskFactory(new FtpDownloadTaskFactory());

        List<FTPDownloadTask> ftpDownloadTasks= new ArrayList<>();
        List<FTPTaskBean> ftpTaskBeans = ftpTaskService.getFtpTaskBeans();
        if (ftpTaskBeans != null) {
            for (FTPTaskBean ftpTaskBean : ftpTaskBeans) {
                int state = ftpTaskBean.getState();
                FTPDownloadTask ftpDownloadTask;
                if(state == FTPTaskBean.STATE_IDLE){
                    ftpDownloadTask = new FTPDownloadTask(ftpTaskBean.getSaveDir(),
                            ftpTaskBean.getTargetFileName(),
                            ftpTaskBean.getCreateTime(),
                            Status.IDLE,
                            null,
                            ftpTaskBean.getCurrentLength(),
                            ftpTaskBean.getHost(),
                            ftpTaskBean.getPort(),
                            ftpTaskBean.getUserName(),
                            ftpTaskBean.getPassword(),
                            ftpTaskBean.getFtpDir(),
                            ftpTaskBean.getFtpFileName(),
                            null,
                            ftpTaskBean.getSaveFileName()
                    );
                }else if(state == FTPTaskBean.STATE_DOWNLOADING){
                    ftpDownloadTask = new FTPDownloadTask(ftpTaskBean.getSaveDir(),
                            ftpTaskBean.getTargetFileName(),
                            ftpTaskBean.getCreateTime(),
                            Status.DOWNLOADING,
                            null,
                            ftpTaskBean.getCurrentLength(),
                            ftpTaskBean.getHost(),
                            ftpTaskBean.getPort(),
                            ftpTaskBean.getUserName(),
                            ftpTaskBean.getPassword(),
                            ftpTaskBean.getFtpDir(),
                            ftpTaskBean.getFtpFileName(),
                            makeResourceInfo(ftpTaskBean),
                            ftpTaskBean.getSaveFileName()
                    );
                    ftpDownloadTask.forceStart();
                }else if(state == FTPTaskBean.STATE_SUCCESS){
                    ftpDownloadTask = new FTPDownloadTask(ftpTaskBean.getSaveDir(),
                            ftpTaskBean.getTargetFileName(),
                            ftpTaskBean.getCreateTime(),
                            Status.SUCCESS,
                            null,
                            ftpTaskBean.getCurrentLength(),
                            ftpTaskBean.getHost(),
                            ftpTaskBean.getPort(),
                            ftpTaskBean.getUserName(),
                            ftpTaskBean.getPassword(),
                            ftpTaskBean.getFtpDir(),
                            ftpTaskBean.getFtpFileName(),
                            makeResourceInfo(ftpTaskBean),
                            ftpTaskBean.getSaveFileName()
                    );
                }else if(state == FTPTaskBean.STATE_FAIL){
                    ftpDownloadTask = new FTPDownloadTask(ftpTaskBean.getSaveDir(),
                            ftpTaskBean.getTargetFileName(),
                            ftpTaskBean.getCreateTime(),
                            Status.FAIL,
                            ftpTaskBean.getErrorMsg(),
                            ftpTaskBean.getCurrentLength(),
                            ftpTaskBean.getHost(),
                            ftpTaskBean.getPort(),
                            ftpTaskBean.getUserName(),
                            ftpTaskBean.getPassword(),
                            ftpTaskBean.getFtpDir(),
                            ftpTaskBean.getFtpFileName(),
                            makeResourceInfo(ftpTaskBean),
                            ftpTaskBean.getSaveFileName()
                    );
                }else if(state == FTPTaskBean.STATE_CANCEL){
                    ftpDownloadTask = new FTPDownloadTask(ftpTaskBean.getSaveDir(),
                            ftpTaskBean.getTargetFileName(),
                            ftpTaskBean.getCreateTime(),
                            Status.CANCELED,
                            null,
                            ftpTaskBean.getCurrentLength(),
                            ftpTaskBean.getHost(),
                            ftpTaskBean.getPort(),
                            ftpTaskBean.getUserName(),
                            ftpTaskBean.getPassword(),
                            ftpTaskBean.getFtpDir(),
                            ftpTaskBean.getFtpFileName(),
                            makeResourceInfo(ftpTaskBean),
                            ftpTaskBean.getSaveFileName()
                    );
                }else {
                    throw new IllegalStateException("unknown state:"+state);
                }
                handleFtpDownloadTaskSave(ftpDownloadTask,ftpTaskBean);
                ftpDownloadTasks.add(ftpDownloadTask);
            }
        }
        downloadManager.addDownloadTasks(ftpDownloadTasks);
    }

    @Override
    public void drop() {

    }

    @Override
    public String getName() {
        return "ftp 下载支持插件";
    }


    class FtpDownloadTaskFactory implements DownloadTaskFactory{

        @Override
        public DownloadTask createTask(DownloadRequest downloadRequest) {
            if(downloadRequest instanceof FtpDownloadRequest){
                FtpDownloadRequest ftpDownloadRequest = (FtpDownloadRequest) downloadRequest;
                FTPDownloadTask ftpDownloadTask = new FTPDownloadTask(ftpDownloadRequest.getSaveDir(),
                        ftpDownloadRequest.getTargetFileName(),
                        System.currentTimeMillis(),
                        ftpDownloadRequest.getHost(),
                        ftpDownloadRequest.getPort(),
                        ftpDownloadRequest.getUsername(),
                        ftpDownloadRequest.getPassword(),
                        ftpDownloadRequest.getFtpDir(),
                        ftpDownloadRequest.getFtpFileName());

                FTPTaskBean ftpTaskBean = new FTPTaskBean();
                ftpTaskBean.setId(UUID.randomUUID().toString());
                ftpTaskBean.setSaveDir(ftpDownloadTask.getSaveDir());
                ftpTaskBean.setTargetFileName(ftpDownloadTask.getTargetFileName());
                ftpTaskBean.setSaveFileName(ftpDownloadTask.getSaveFileName());
                ftpTaskBean.setCreateTime(ftpDownloadTask.getCreateTime());
                ftpTaskBean.setHost(ftpDownloadTask.getHost());
                ftpTaskBean.setPort(ftpDownloadTask.getPort());
                ftpTaskBean.setUserName(ftpDownloadTask.getUserName());
                ftpTaskBean.setPassword(ftpDownloadTask.getPassword());
                ftpTaskBean.setFtpDir(ftpDownloadTask.getFtpDir());
                ftpTaskBean.setFtpFileName(ftpDownloadTask.getFtpFileName());
                ftpTaskBean.setState(FTPTaskBean.STATE_IDLE);
                ftpTaskService.addFtpTask(ftpTaskBean);

                handleFtpDownloadTaskSave(ftpDownloadTask,ftpTaskBean);
                return ftpDownloadTask;
            }
            return null;
        }
    }

    private void handleFtpDownloadTaskSave(FTPDownloadTask ftpDownloadTask, FTPTaskBean ftpTaskBean) {
        ftpDownloadTask.addOnResourceInfoReadyListener(new OnResourceInfoReadyListener() {
            @Override
            public void onResourceInfoReady(ResourceInfo resourceInfo) {
                ftpTaskBean.setContentLength(resourceInfo.getContentLength());
                ftpTaskBean.setContentType(resourceInfo.getContentType());
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }
        });
        ftpDownloadTask.addTaskFailListener(new OnTaskFailListener() {
            @Override
            public void onTaskFail(Throwable e) {
                ftpTaskBean.setState(FTPTaskBean.STATE_FAIL);
                ftpTaskBean.setErrorMsg(e.getMessage());
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }
        });
        ftpDownloadTask.addStatusChangeListener(new OnStatusChangeListener() {
            @Override
            public void onStatusChange(Status newStatus, Status oldStatus) {
                if (newStatus == Status.IDLE) {
                    ftpTaskBean.setState(FTPTaskBean.STATE_IDLE);
                    ftpTaskBean.setErrorMsg(null);
                    ftpTaskService.updateFtpTask(ftpTaskBean);
                }else if (newStatus == Status.DOWNLOADING) {
                    ftpTaskBean.setState(FTPTaskBean.STATE_DOWNLOADING);
                    ftpTaskBean.setErrorMsg(null);
                    ftpTaskService.updateFtpTask(ftpTaskBean);
                }else if (newStatus == Status.SUCCESS) {
                    ftpTaskBean.setState(FTPTaskBean.STATE_SUCCESS);
                    ftpTaskBean.setErrorMsg(null);
                    ftpTaskService.updateFtpTask(ftpTaskBean);
                }else if (newStatus == Status.CANCELED) {
                    ftpTaskBean.setState(FTPTaskBean.STATE_CANCEL);
                    ftpTaskBean.setErrorMsg(null);
                    ftpTaskService.updateFtpTask(ftpTaskBean);
                }
            }
        });
        ftpDownloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
            @Override
            public void onProgressChange() {
                ftpTaskBean.setCurrentLength(ftpDownloadTask.getCurrentLength());
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }
        });
    }

    private static ResourceInfo makeResourceInfo(FTPTaskBean ftpTaskBean){
        return new ResourceInfo(ftpTaskBean.getContentLength(),ftpTaskBean.getContentType());
    }
}

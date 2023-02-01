package com.zhangqiang.downloadmanager.task.ftp.support;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.support.DownloadBundle;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.support.LocalTask;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.bean.FTPTaskBean;
import com.zhangqiang.downloadmanager.task.ftp.callback.Callback;
import com.zhangqiang.downloadmanager.task.ftp.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.task.ftp.request.FTPDownloadRequest;
import com.zhangqiang.downloadmanager.task.ftp.service.FTPTaskService;
import com.zhangqiang.downloadmanager.task.http.support.HttpTaskInfo;
import com.zhangqiang.downloadmanager.task.speed.SpeedUtils;
import com.zhangqiang.downloadmanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FTPDownloadSupport implements DownloadSupport {

    private final FTPTaskService ftpTaskService;

    public FTPDownloadSupport(Context context) {
        ftpTaskService = new FTPTaskService(context);
    }

    @Override
    public List<LocalTask> loadLocalTasks() {
        List<FTPTaskBean> ftpTasks = ftpTaskService.getFtpTasks();
        if (ftpTasks != null) {
            List<LocalTask> localTasks = new ArrayList<>();
            for (FTPTaskBean ftpTask : ftpTasks) {
                localTasks.add(new LocalTask(ftpTask.getId(),
                        createTask(ftpTask),
                         ftpTask.getState() == FTPTaskBean.STATE_DOWNLOADING));
            }
            return localTasks;
        }
        return null;
    }

    @Override
    public DownloadBundle createDownloadBundle(String id, DownloadRequest request) {
        if(request instanceof FTPDownloadRequest){
            FTPDownloadRequest ftpRequest = (FTPDownloadRequest) request;
            FTPTaskBean ftpTaskBean = new FTPTaskBean();
            ftpTaskBean.setId(id);
            ftpTaskBean.setHost(ftpRequest.getHost());
            ftpTaskBean.setPort(ftpRequest.getPort());
            ftpTaskBean.setUserName(ftpRequest.getUserName());
            ftpTaskBean.setPassword(ftpRequest.getPassword());
            ftpTaskBean.setFtpDir(ftpRequest.getFtpDir());
            String ftpFileName = ftpRequest.getFtpFileName();
            ftpTaskBean.setFtpFileName(ftpFileName);
            ftpTaskBean.setSaveDir(ftpRequest.getSaveDir());
            String fileName = ftpRequest.getFileName();
            ftpTaskBean.setTargetFileName(fileName);
            if(!TextUtils.isEmpty(fileName)){
                ftpTaskBean.setFileName(fileName);
            }else {
                ftpTaskBean.setFileName(ftpTaskBean.getFtpFileName());
            }
            ftpTaskBean.setState(FTPTaskBean.STATE_IDLE);
            ftpTaskBean.setCreateTime(new Date());
            ftpTaskBean.setCurrentLength(0);

            ftpTaskService.addFtpTask(ftpTaskBean);
            return createTask(ftpTaskBean);
        }
        return null;
    }

    private DownloadBundle createTask(FTPTaskBean ftpTaskBean) {
        InternalTask ftpDownloadTask = new InternalTask(
                ftpTaskBean.getHost(),
                ftpTaskBean.getPort(),
                ftpTaskBean.getUserName(),
                ftpTaskBean.getPassword(),
                ftpTaskBean.getFtpDir(),
                ftpTaskBean.getFtpFileName(),
                ftpTaskBean.getSaveDir(),
                ftpTaskBean.getFileName());
        FTPTaskInfoImpl taskInfo = new FTPTaskInfoImpl(ftpTaskBean, ftpDownloadTask);
        ftpDownloadTask.ftpTaskBean = ftpTaskBean;
        ftpDownloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onReset() {
                ftpTaskBean.setState(FTPTaskBean.STATE_IDLE);
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyStateChanged();
            }

            @Override
            public void onStart() {
                ftpTaskBean.setState(FTPTaskBean.STATE_DOWNLOADING);
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyStateChanged();
            }

            @Override
            public void onComplete() {
                ftpTaskBean.setState(FTPTaskBean.STATE_SUCCESS);
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyStateChanged();
            }

            @Override
            public void onFail(DownloadException e) {
                ftpTaskBean.setErrorMsg(e.getMessage());
                ftpTaskBean.setState(FTPTaskBean.STATE_FAIL);
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyStateChanged();
            }

            @Override
            public void onCancel() {
                ftpTaskBean.setState(FTPTaskBean.STATE_CANCEL);
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyStateChanged();
            }
        });
        ftpDownloadTask.getCallbacks().addCallback(new Callback() {
            @Override
            public void onResourceInfoReady(ResourceInfo resourceInfo) {
                ftpTaskBean.setContentLength(resourceInfo.getContentLength());
                ftpTaskBean.setContentType(resourceInfo.getContentType());
                ftpTaskService.updateFtpTask(ftpTaskBean);
                taskInfo.getListeners().notifyInfoReady();
            }
        });
        return new DownloadBundle(ftpDownloadTask,taskInfo);
    }

    @Override
    public void handleProgressSync(DownloadBundle downloadBundle) {
        DownloadTask downloadTask = downloadBundle.getDownloadTask();
        FTPTaskInfoImpl taskInfo = ((FTPTaskInfoImpl) downloadBundle.getTaskInfo());
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
        long currentLength = downloadTask.getCurrentLength();
        long oldLength = ftpTaskBean.getCurrentLength();
        if(oldLength != currentLength){
            ftpTaskBean.setCurrentLength(currentLength);
            ftpTaskService.updateFtpTask(ftpTaskBean);
            taskInfo.getListeners().notifyProgressChanged();
        }
    }

    @Override
    public void handleSpeedCompute(DownloadBundle downloadBundle) {
        DownloadTask downloadTask = downloadBundle.getDownloadTask();
        FTPTaskInfoImpl taskInfo = (FTPTaskInfoImpl) downloadBundle.getTaskInfo();
        if (SpeedUtils.computeSpeed(downloadTask)) {
            taskInfo.getListeners().notifySeedChanged();
        }
    }

    @Override
    public boolean isTaskIdle(DownloadBundle downloadBundle) {
        DownloadTask downloadTask = downloadBundle.getDownloadTask();
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
        return ftpTaskBean.getState() == FTPTaskBean.STATE_IDLE;
    }

    @Override
    public void handleDeleteTask(DownloadBundle downloadBundle, boolean deleteFile) {
        DownloadTask downloadTask = downloadBundle.getDownloadTask();
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
        ftpTaskService.removeFtpTask(ftpTaskBean);
        File file = new File(ftpTaskBean.getSaveDir(), ftpTaskBean.getFileName());
        try {
            FileUtils.deleteFileIfExists(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class InternalTask extends FTPDownloadTask{
       private  FTPTaskBean ftpTaskBean;

        public InternalTask(String host, int port, String userName, String password, String ftpDir, String ftpFileName, String saveDir, String fileName) {
            super(host, port, userName, password, ftpDir, ftpFileName, saveDir, fileName);
        }
    }
}

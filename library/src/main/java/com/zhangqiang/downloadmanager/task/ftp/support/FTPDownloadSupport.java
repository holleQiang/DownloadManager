package com.zhangqiang.downloadmanager.task.ftp.support;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.support.LocalTask;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.bean.FTPTaskBean;
import com.zhangqiang.downloadmanager.task.ftp.request.FTPDownloadRequest;
import com.zhangqiang.downloadmanager.task.ftp.service.FTPTaskService;
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
    public DownloadTask createDownloadTask(String id, DownloadRequest request) {
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

    private DownloadTask createTask(FTPTaskBean ftpTaskBean) {
        InternalTask ftpDownloadTask = new InternalTask(
                ftpTaskBean.getHost(),
                ftpTaskBean.getPort(),
                ftpTaskBean.getUserName(),
                ftpTaskBean.getPassword(),
                ftpTaskBean.getFtpDir(),
                ftpTaskBean.getFtpFileName(),
                ftpTaskBean.getSaveDir(),
                ftpTaskBean.getFileName());
        ftpDownloadTask.ftpTaskBean = ftpTaskBean;
        ftpDownloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onReset() {
                ftpTaskBean.setState(FTPTaskBean.STATE_IDLE);
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }

            @Override
            public void onStart() {
                ftpTaskBean.setState(FTPTaskBean.STATE_DOWNLOADING);
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }

            @Override
            public void onComplete() {
                ftpTaskBean.setState(FTPTaskBean.STATE_SUCCESS);
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }

            @Override
            public void onFail(DownloadException e) {
                ftpTaskBean.setErrorMsg(e.getMessage());
                ftpTaskBean.setState(FTPTaskBean.STATE_FAIL);
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }

            @Override
            public void onCancel() {
                ftpTaskBean.setState(FTPTaskBean.STATE_CANCEL);
                ftpTaskService.updateFtpTask(ftpTaskBean);
            }
        });
        return ftpDownloadTask;
    }

    @Override
    public TaskInfo buildTaskInfo(DownloadTask downloadTask) {
        InternalTask internalTask = (InternalTask) downloadTask;
        FTPTaskBean ftpTaskBean = internalTask.ftpTaskBean;
        return new TaskInfo() {
            @Override
            public String getId() {
                return ftpTaskBean.getId();
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public String getSaveDir() {
                return ftpTaskBean.getSaveDir();
            }

            @Override
            public String getFileName() {
                return ftpTaskBean.getFileName();
            }

            @Override
            public long getCurrentLength() {
                return ftpTaskBean.getCurrentLength();
            }

            @Override
            public long getContentLength() {
                return 0;
            }

            @Override
            public int getState() {
                int state = ftpTaskBean.getState();
                if(state == FTPTaskBean.STATE_IDLE){
                    return STATE_IDLE;
                }else if(state == FTPTaskBean.STATE_DOWNLOADING){
                    return STATE_DOWNLOADING;
                }else if(state == FTPTaskBean.STATE_SUCCESS){
                    return STATE_COMPLETE;
                }else if(state == FTPTaskBean.STATE_FAIL){
                    return STATE_FAIL;
                }else if(state == FTPTaskBean.STATE_CANCEL){
                    return STATE_PAUSE;
                }
                return 0;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public long getCreateTime() {
                return ftpTaskBean.getCreateTime().getTime();
            }

            @Override
            public String getErrorMsg() {
                return ftpTaskBean.getErrorMsg();
            }

            @Override
            public int getThreadCount() {
                return 0;
            }

            @Override
            public long getSpeed() {
                return SpeedUtils.getSpeed(downloadTask);
            }

            @Override
            public int getPartCount() {
                return 0;
            }

            @Override
            public long getPartSpeed(int partIndex) {
                return 0;
            }

            @Override
            public long getPartCurrentLength(int partIndex) {
                return 0;
            }

            @Override
            public long getPartContentLength(int partIndex) {
                return 0;
            }
        };
    }

    @Override
    public boolean handleProgressSync(DownloadTask downloadTask) {
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
        long currentLength = downloadTask.getCurrentLength();
        long oldLength = ftpTaskBean.getCurrentLength();
        if(oldLength != currentLength){
            ftpTaskBean.setCurrentLength(currentLength);
            ftpTaskService.updateFtpTask(ftpTaskBean);
            return true;
        }
        return false;
    }

    @Override
    public boolean handleSpeedCompute(DownloadTask downloadTask) {
        return SpeedUtils.computeSpeed(downloadTask);
    }

    @Override
    public boolean isTaskIdle(DownloadTask downloadTask) {
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
        return ftpTaskBean.getState() == FTPTaskBean.STATE_IDLE;
    }

    @Override
    public void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile) {
        FTPTaskBean ftpTaskBean = ((InternalTask) downloadTask).ftpTaskBean;
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

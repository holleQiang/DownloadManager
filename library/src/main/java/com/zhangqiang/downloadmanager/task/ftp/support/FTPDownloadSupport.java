package com.zhangqiang.downloadmanager.task.ftp.support;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.bean.FTPTaskBean;
import com.zhangqiang.downloadmanager.task.ftp.request.FTPDownloadRequest;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FTPDownloadSupport implements DownloadSupport {

    private final HashMap<String, FTPTaskBean> ftpTaskBeanHashMap = new HashMap<>();

    @Override
    public List<DownloadTask> loadDownloadTasks() {
        return null;
    }

    @Override
    public DownloadTask createDownloadTask(DownloadRequest request) {
        if(request instanceof FTPDownloadRequest){
            FTPDownloadRequest ftpDownloadRequest = (FTPDownloadRequest) request;
            String taskId = UUID.randomUUID().toString();
            FTPDownloadTask ftpDownloadTask = new FTPDownloadTask(taskId,
                    ftpDownloadRequest.getHost(),
                    ftpDownloadRequest.getPort(),
                    ftpDownloadRequest.getUserName(),
                    ftpDownloadRequest.getPassword(),
                    ftpDownloadRequest.getFtpDir(),
                    ftpDownloadRequest.getFtpFileName(),
                    ftpDownloadRequest.getSaveDir(),
                    ftpDownloadRequest.getFileName());
            ftpDownloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
                @Override
                public void onReset() {
                    ftpTaskBeanHashMap.get(taskId).setState(FTPTaskBean.STATE_IDLE);
                }

                @Override
                public void onStart() {
                    ftpTaskBeanHashMap.get(taskId).setState(FTPTaskBean.STATE_DOWNLOADING);
                }

                @Override
                public void onComplete() {
                    ftpTaskBeanHashMap.get(taskId).setState(FTPTaskBean.STATE_SUCCESS);
                }

                @Override
                public void onFail(DownloadException e) {
                    FTPTaskBean ftpTaskBean = ftpTaskBeanHashMap.get(taskId);
                    ftpTaskBean.setErrorMsg(e.getMessage());
                    ftpTaskBean.setState(FTPTaskBean.STATE_FAIL);
                }

                @Override
                public void onCancel() {
                    ftpTaskBeanHashMap.get(taskId).setState(FTPTaskBean.STATE_CANCEL);
                }
            });
            ftpTaskBeanHashMap.put(taskId,new FTPTaskBean());
            return ftpDownloadTask;
        }
        return null;
    }

    @Override
    public TaskInfo buildTaskInfo(DownloadTask downloadTask) {
        return new TaskInfo() {
            @Override
            public String getId() {
                return downloadTask.getId();
            }

            @Override
            public String getUrl() {
                return null;
            }

            @Override
            public String getSaveDir() {
                return null;
            }

            @Override
            public String getFileName() {
                return null;
            }

            @Override
            public long getCurrentLength() {
                return downloadTask.getCurrentLength();
            }

            @Override
            public long getContentLength() {
                return 0;
            }

            @Override
            public int getState() {
                FTPTaskBean ftpTaskBean = ftpTaskBeanHashMap.get(downloadTask.getId());
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
                return 0;
            }

            @Override
            public String getErrorMsg() {
                FTPTaskBean ftpTaskBean = ftpTaskBeanHashMap.get(downloadTask.getId());
                return ftpTaskBean.getErrorMsg();
            }

            @Override
            public int getThreadCount() {
                return 0;
            }

            @Override
            public long getSpeed() {
                return 0;
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
        return false;
    }

    @Override
    public boolean handleSpeedCompute(DownloadTask downloadTask) {
        return false;
    }

    @Override
    public boolean isTaskIdle(DownloadTask downloadTask) {
        return ftpTaskBeanHashMap.get(downloadTask.getId()).getState() == FTPTaskBean.STATE_IDLE;
    }

    @Override
    public boolean isTaskRunning(DownloadTask downloadTask) {
        return ftpTaskBeanHashMap.get(downloadTask.getId()).getState() == FTPTaskBean.STATE_DOWNLOADING;
    }

    @Override
    public void handleDeleteTask(DownloadTask downloadTask, boolean deleteFile) {

    }
}

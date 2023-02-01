package com.zhangqiang.downloadmanager.task.ftp.support;

import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.bean.FTPTaskBean;
import com.zhangqiang.downloadmanager.task.speed.SpeedUtils;

public class FTPTaskInfoImpl implements FTPTaskInfo{

    private final FTPTaskBean ftpTaskBean;
    private final FTPDownloadTask ftpDownloadTask;
    private final Listeners listeners = new Listeners();

    public FTPTaskInfoImpl(FTPTaskBean ftpTaskBean, FTPDownloadTask ftpDownloadTask) {
        this.ftpTaskBean = ftpTaskBean;
        this.ftpDownloadTask = ftpDownloadTask;
    }

    @Override
    public String getId() {
        return ftpTaskBean.getId();
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
        return ftpTaskBean.getContentLength();
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
    public long getCreateTime() {
        return ftpTaskBean.getCreateTime().getTime();
    }

    @Override
    public String getContentType() {
        return ftpTaskBean.getContentType();
    }

    @Override
    public String getErrorMsg() {
        return ftpTaskBean.getErrorMsg();
    }

    @Override
    public long getSpeed() {
        return SpeedUtils.getSpeed(ftpDownloadTask);
    }

    @Override
    public String getHost() {
        return ftpTaskBean.getHost();
    }

    @Override
    public int getPort() {
        return ftpTaskBean.getPort();
    }

    @Override
    public String getUserName() {
        return ftpTaskBean.getUserName();
    }

    @Override
    public String getPassword() {
        return ftpTaskBean.getPassword();
    }

    @Override
    public String getFtpDir() {
        return ftpTaskBean.getFtpDir();
    }

    @Override
    public String getFtpFileName() {
        return ftpTaskBean.getFtpFileName();
    }

    @Override
    public void addListener(Listener listener) {
        getListeners().addListener(listener);
    }

    @Override
    public void removeListener(Listener listener) {
        getListeners().removeListener(listener);
    }

    public Listeners getListeners() {
        return listeners;
    }
}

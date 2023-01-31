package com.zhangqiang.downloadmanager.task.ftp.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.FTPTaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.FTPTaskEntity;
import com.zhangqiang.downloadmanager.task.ftp.bean.FTPTaskBean;

import java.util.ArrayList;
import java.util.List;

public class FTPTaskService {

    private final FTPTaskEntityDao ftpTaskEntityDao;

    public FTPTaskService(Context context) {
        ftpTaskEntityDao = new DBManager(context).getDaoSession().getFTPTaskEntityDao();
    }

    public void addFtpTask(FTPTaskBean ftpTaskBean) {
        FTPTaskEntity ftpTaskEntity = toEntity(ftpTaskBean);
        ftpTaskEntityDao.insert(ftpTaskEntity);
    }

    private FTPTaskEntity toEntity(FTPTaskBean ftpTaskBean) {
        FTPTaskEntity ftpTaskEntity = new FTPTaskEntity();
        ftpTaskEntity.setId(ftpTaskBean.getId());
        ftpTaskEntity.setHost(ftpTaskBean.getHost());
        ftpTaskEntity.setPort(ftpTaskBean.getPort());
        ftpTaskEntity.setUserName(ftpTaskBean.getUserName());
        ftpTaskEntity.setPassword(ftpTaskBean.getPassword());
        ftpTaskEntity.setFtpDir(ftpTaskBean.getFtpDir());
        ftpTaskEntity.setFtpFileName(ftpTaskBean.getFtpFileName());
        ftpTaskEntity.setSaveDir(ftpTaskBean.getSaveDir());
        ftpTaskEntity.setTargetFileName(ftpTaskBean.getTargetFileName());
        ftpTaskEntity.setFileName(ftpTaskBean.getFileName());
        ftpTaskEntity.setCreateTime(ftpTaskBean.getCreateTime());
        ftpTaskEntity.setCurrentLength(ftpTaskBean.getCurrentLength());
        ftpTaskEntity.setState(ftpTaskBean.getState());
        ftpTaskEntity.setErrorMsg(ftpTaskBean.getErrorMsg());
        ftpTaskEntity.setContentLength(ftpTaskBean.getContentLength());
        return ftpTaskEntity;
    }

    public List<FTPTaskBean> getFtpTasks() {
        List<FTPTaskEntity> entities = ftpTaskEntityDao.queryBuilder().orderDesc(FTPTaskEntityDao.Properties.CreateTime)
                .list();
        if (entities != null) {
            List<FTPTaskBean> ftpTaskBeans = new ArrayList<>();
            for (FTPTaskEntity entity : entities) {
                FTPTaskBean ftpTaskBean = new FTPTaskBean();
                ftpTaskBean.setId(entity.getId());
                ftpTaskBean.setHost(entity.getHost());
                ftpTaskBean.setPort(entity.getPort());
                ftpTaskBean.setUserName(entity.getUserName());
                ftpTaskBean.setPassword(entity.getPassword());
                ftpTaskBean.setSaveDir(entity.getSaveDir());
                ftpTaskBean.setFileName(entity.getFileName());
                ftpTaskBean.setFtpDir(entity.getFtpDir());
                ftpTaskBean.setFtpFileName(entity.getFtpFileName());
                ftpTaskBean.setTargetFileName(entity.getTargetFileName());
                ftpTaskBean.setCreateTime(entity.getCreateTime());
                ftpTaskBean.setCurrentLength(entity.getCurrentLength());
                ftpTaskBean.setState(entity.getState());
                ftpTaskBean.setErrorMsg(entity.getErrorMsg());
                ftpTaskBean.setContentLength(entity.getContentLength());
                ftpTaskBeans.add(ftpTaskBean);
            }
            return ftpTaskBeans;
        }
        return null;
    }

    public void updateFtpTask(FTPTaskBean ftpTaskBean) {
        ftpTaskEntityDao.update(toEntity(ftpTaskBean));
    }

    public void removeFtpTask(FTPTaskBean ftpTaskBean) {
        ftpTaskEntityDao.deleteByKey(ftpTaskBean.getId());
    }
}

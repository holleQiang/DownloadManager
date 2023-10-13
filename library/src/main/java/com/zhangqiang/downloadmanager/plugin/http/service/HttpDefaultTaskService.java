package com.zhangqiang.downloadmanager.plugin.http.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.HttpDefaultTaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.HttpDefaultTaskEntity;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpDefaultTaskBean;

public class HttpDefaultTaskService {

    private final DBManager mDBManager;

    public HttpDefaultTaskService(Context context) {
        this.mDBManager = new DBManager(context);
    }

    public HttpDefaultTaskBean get(String id) {
        HttpDefaultTaskEntity defaultTaskEntity = getHttpDefaultTaskEntityDao().load(id);
        HttpDefaultTaskBean httpDefaultTask = new HttpDefaultTaskBean();
        httpDefaultTask.setCreateTime(defaultTaskEntity.getCreateTime());
        httpDefaultTask.setErrorMsg(defaultTaskEntity.getErrorMsg());
        httpDefaultTask.setState(defaultTaskEntity.getState());
        httpDefaultTask.setId(defaultTaskEntity.getId());
        httpDefaultTask.setCurrentLength(defaultTaskEntity.getCurrentLength());
        return httpDefaultTask;
    }

    public void add(HttpDefaultTaskBean httpDefaultTaskBean) {
        getHttpDefaultTaskEntityDao().insert(beanToEntity(httpDefaultTaskBean));
    }

    public void update(HttpDefaultTaskBean httpDefaultTaskBean) {
        getHttpDefaultTaskEntityDao().update(beanToEntity(httpDefaultTaskBean));
    }

    private HttpDefaultTaskEntityDao getHttpDefaultTaskEntityDao() {
        return mDBManager.getDaoSession().getHttpDefaultTaskEntityDao();
    }

    private static HttpDefaultTaskEntity beanToEntity(HttpDefaultTaskBean httpDefaultTask) {
        HttpDefaultTaskEntity httpDefaultTaskEntity = new HttpDefaultTaskEntity();
        httpDefaultTaskEntity.setId(httpDefaultTask.getId());
        httpDefaultTaskEntity.setCreateTime(httpDefaultTask.getCreateTime());
        httpDefaultTaskEntity.setState(httpDefaultTask.getState());
        httpDefaultTaskEntity.setCurrentLength(httpDefaultTask.getCurrentLength());
        httpDefaultTaskEntity.setErrorMsg(httpDefaultTask.getErrorMsg());
        return httpDefaultTaskEntity;
    }

    public void remove(String id) {
        getHttpDefaultTaskEntityDao().deleteByKey(id);
    }
}

package com.zhangqiang.downloadmanager.task.http.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.HttpDefaultTaskEntityDao;
import com.zhangqiang.downloadmanager.db.dao.HttpPartTaskEntityDao;
import com.zhangqiang.downloadmanager.db.dao.HttpPartTaskItemEntityDao;
import com.zhangqiang.downloadmanager.db.dao.HttpTaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.HttpDefaultTaskEntity;
import com.zhangqiang.downloadmanager.db.entity.HttpPartTaskEntity;
import com.zhangqiang.downloadmanager.db.entity.HttpPartTaskItemEntity;
import com.zhangqiang.downloadmanager.db.entity.HttpTaskEntity;
import com.zhangqiang.downloadmanager.task.http.bean.HttpDefaultTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpTaskBean;

import java.util.ArrayList;
import java.util.List;

public class HttpTaskService {

    private final DBManager mDBManager;
    private final HttpPartTaskService httpPartTaskService;
    private final HttpDefaultTaskService httpDefaultTaskService;

    public HttpTaskService(Context context) {
        this.mDBManager = new DBManager(context);
        httpPartTaskService = new HttpPartTaskService(context);
        httpDefaultTaskService = new HttpDefaultTaskService(context);
    }

    public HttpTaskBean getHttpTask(String id) {
        return new HttpTaskBean();
    }

    public List<HttpTaskBean> getHttpTasks() {

        List<HttpTaskEntity> httpTaskEntities = getHttpTaskEntityDao().queryBuilder()
                .list();
        if (httpTaskEntities == null) {
            return null;
        }
        List<HttpTaskBean> httpTaskBeans = new ArrayList<>();
        for (HttpTaskEntity httpTaskEntity : httpTaskEntities) {
            HttpTaskBean httpTaskBean = new HttpTaskBean();
            httpTaskBean.setId(httpTaskEntity.getId());
            httpTaskBean.setUrl(httpTaskEntity.getUrl());
            httpTaskBean.setSaveDir(httpTaskEntity.getSaveDir());
            httpTaskBean.setFileName(httpTaskEntity.getFileName());
            httpTaskBean.setContentLength(httpTaskEntity.getContentLength());
            httpTaskBean.setContentType(httpTaskEntity.getContentType());
            httpTaskBean.setCreateTime(httpTaskEntity.getCreateTime());
            httpTaskBean.setState(httpTaskEntity.getState());
            httpTaskBean.setErrorMsg(httpTaskEntity.getErrorMsg());
            httpTaskBean.setThreadSize(httpTaskEntity.getThreadSize());
            int httpTaskType = httpTaskEntity.getType();
            httpTaskBean.setType(httpTaskType);
            String childId = httpTaskEntity.getChildId();
            if (httpTaskType == HttpTaskBean.TYPE_DEFAULT) {
                httpTaskBean.setHttpDefaultTask(httpDefaultTaskService.get(childId));
            } else if (httpTaskType == HttpTaskBean.TYPE_PART) {
                httpTaskBean.setHttpPartTask(httpPartTaskService.get(childId));
            }
            httpTaskBeans.add(httpTaskBean);
        }
        return httpTaskBeans;
    }

    public void add(HttpTaskBean httpTaskBean) {
        getHttpTaskEntityDao().insert(beanToEntity(httpTaskBean));
    }

    public void update(HttpTaskBean httpTaskBean) {
        getHttpTaskEntityDao().update(beanToEntity(httpTaskBean));
    }

    private static HttpTaskEntity beanToEntity(HttpTaskBean httpTaskBean) {
        HttpTaskEntity httpTaskEntity = new HttpTaskEntity();
        httpTaskEntity.setId(httpTaskBean.getId());
        httpTaskEntity.setSaveDir(httpTaskBean.getSaveDir());
        httpTaskEntity.setThreadSize(httpTaskBean.getThreadSize());
        httpTaskEntity.setState(httpTaskBean.getState());
        httpTaskEntity.setContentLength(httpTaskBean.getContentLength());
        httpTaskEntity.setUrl(httpTaskBean.getUrl());
        httpTaskEntity.setCreateTime(httpTaskBean.getCreateTime());
        httpTaskEntity.setContentType(httpTaskBean.getContentType());
        httpTaskEntity.setFileName(httpTaskBean.getFileName());
        httpTaskEntity.setErrorMsg(httpTaskBean.getErrorMsg());
        int type = httpTaskBean.getType();
        httpTaskEntity.setType(type);
        if (type == HttpTaskBean.TYPE_DEFAULT) {
            HttpDefaultTaskBean httpDefaultTask = httpTaskBean.getHttpDefaultTask();
            httpTaskEntity.setChildId(httpDefaultTask.getId());
        } else if (type == HttpTaskBean.TYPE_PART) {
            HttpPartTaskBean httpPartTask = httpTaskBean.getHttpPartTask();
            httpTaskEntity.setChildId(httpPartTask.getId());
        }
        return httpTaskEntity;
    }


    private HttpTaskEntityDao getHttpTaskEntityDao() {
        return mDBManager.getDaoSession().getHttpTaskEntityDao();
    }


}

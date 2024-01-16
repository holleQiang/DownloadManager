package com.zhangqiang.downloadmanager.plugin.m3u8.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.TSTaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.TSTaskEntity;
import com.zhangqiang.downloadmanager.plugin.http.bean.HttpPartTaskItemBean;
import com.zhangqiang.downloadmanager.plugin.http.service.HttpPartTaskItemService;
import com.zhangqiang.downloadmanager.plugin.m3u8.bean.TSTaskBean;

import java.util.ArrayList;
import java.util.List;

public class TSTaskService {


    private final DBManager mDBManager;
    private final HttpPartTaskItemService partTaskItemService;

    public TSTaskService(Context context) {
        this.mDBManager = new DBManager(context);
        partTaskItemService = new HttpPartTaskItemService(context);
    }

    public List<TSTaskBean> getTSTaskBeans(List<String> ids){
        TSTaskEntityDao tsTaskEntityDao = getTsTaskEntityDao();
        List<TSTaskEntity> list = tsTaskEntityDao.queryBuilder().where(TSTaskEntityDao.Properties.Id.in(ids)).list();
        if (list != null) {
            List<TSTaskBean> beans = new ArrayList<>();
            for (TSTaskEntity entity : list) {
                TSTaskBean tsTaskBean = new TSTaskBean();
                tsTaskBean.setId(entity.getId());
                tsTaskBean.setUri(entity.getUri());
                tsTaskBean.setDuration(entity.getDuration());
                tsTaskBean.setHttpPartTaskItemBean(partTaskItemService.get(entity.getChildId()));
                beans.add(tsTaskBean);
            }
            return  beans;
        }
        return null;
    }

    public void add(List<TSTaskBean> beans){
        if (beans == null) {
            return;
        }
        List<TSTaskEntity> entities = new ArrayList<>();
        for (TSTaskBean bean : beans) {
            entities.add(beanToEntity(bean));
        }
        getTsTaskEntityDao().insertInTx(entities);
    }

    private TSTaskEntity beanToEntity(TSTaskBean bean) {
        TSTaskEntity entity = new TSTaskEntity();
        entity.setId(bean.getId());
        entity.setUri(bean.getUri());
        entity.setDuration(bean.getDuration());
        HttpPartTaskItemBean httpPartTaskItemBean = bean.getHttpPartTaskItemBean();
        if (httpPartTaskItemBean != null) {
            entity.setChildId(httpPartTaskItemBean.getId());
        }
        return entity;
    }

    public void update(TSTaskBean bean){
        getTsTaskEntityDao().update(beanToEntity(bean));
    }

    public void remove(List<String> ids){
        getTsTaskEntityDao().deleteByKeyInTx(ids);
    }

    private TSTaskEntityDao getTsTaskEntityDao() {
        return mDBManager.getDaoSession().getTSTaskEntityDao();
    }
}

package com.zhangqiang.downloadmanager.task.http.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.HttpPartTaskItemEntityDao;
import com.zhangqiang.downloadmanager.db.entity.HttpPartTaskItemEntity;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskItemBean;

public class HttpPartTaskItemService {

    private final DBManager mDBManager;

    public HttpPartTaskItemService(Context context) {
        this.mDBManager = new DBManager(context);
    }

    public HttpPartTaskItemBean get(String id) {
        HttpPartTaskItemEntity partTaskItemEntity = getHttpPartTaskItemEntityDao().load(id);
        HttpPartTaskItemBean httpPartTaskItem = new HttpPartTaskItemBean();
        httpPartTaskItem.setId(partTaskItemEntity.getId());
        httpPartTaskItem.setCreateTime(partTaskItemEntity.getCreateTime());
        httpPartTaskItem.setStartPosition(partTaskItemEntity.getStartPosition());
        httpPartTaskItem.setCurrentPosition(partTaskItemEntity.getCurrentPosition());
        httpPartTaskItem.setEndPosition(partTaskItemEntity.getEndPosition());
        httpPartTaskItem.setState(partTaskItemEntity.getState());
        httpPartTaskItem.setFilePath(partTaskItemEntity.getFilePath());
        httpPartTaskItem.setErrorMsg(partTaskItemEntity.getErrorMsg());
        return httpPartTaskItem;
    }

    public void add(HttpPartTaskItemBean taskItemBean) {
        getHttpPartTaskItemEntityDao().insert(beanToEntity(taskItemBean));
    }

    public void update(HttpPartTaskItemBean taskItemBean) {
        getHttpPartTaskItemEntityDao().update(beanToEntity(taskItemBean));
    }

    private HttpPartTaskItemEntityDao getHttpPartTaskItemEntityDao() {
        return mDBManager.getDaoSession().getHttpPartTaskItemEntityDao();
    }

    private static HttpPartTaskItemEntity beanToEntity(HttpPartTaskItemBean item) {
        HttpPartTaskItemEntity itemEntity = new HttpPartTaskItemEntity();
        itemEntity.setId(item.getId());
        itemEntity.setFilePath(item.getFilePath());
        itemEntity.setCreateTime(item.getCreateTime());
        itemEntity.setState(item.getState());
        itemEntity.setStartPosition(item.getStartPosition());
        itemEntity.setCurrentPosition(item.getCurrentPosition());
        itemEntity.setEndPosition(item.getEndPosition());
        itemEntity.setErrorMsg(item.getErrorMsg());
        return itemEntity;
    }
}

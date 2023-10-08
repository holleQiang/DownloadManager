package com.zhangqiang.downloadmanager.task.http.service;

import android.content.Context;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.HttpPartTaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.HttpPartTaskEntity;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskBean;
import com.zhangqiang.downloadmanager.task.http.bean.HttpPartTaskItemBean;

import java.util.ArrayList;
import java.util.List;

public class HttpPartTaskService {

    private final DBManager mDBManager;
    private final HttpPartTaskItemService partTaskItemService;

    public HttpPartTaskService(Context context) {
        this.mDBManager = new DBManager(context);
        partTaskItemService = new HttpPartTaskItemService(context);
    }

    public HttpPartTaskBean get(String id) {
        HttpPartTaskEntity partTaskEntity = getHttpPartTaskEntityDao().load(id);
        if (partTaskEntity == null) {
            return null;
        }
        HttpPartTaskBean httpPartTask = new HttpPartTaskBean();
        httpPartTask.setState(partTaskEntity.getState());
        httpPartTask.setCreateTime(partTaskEntity.getCreateTime());
        httpPartTask.setErrorMsg(partTaskEntity.getErrorMsg());
        httpPartTask.setId(partTaskEntity.getId());
        String itemIdsStr = partTaskEntity.getItemIds();
        if (itemIdsStr != null && itemIdsStr.length() > 0) {
            String[] itemIds = itemIdsStr.split(",");
            List<HttpPartTaskItemBean> partTaskItems = new ArrayList<>();
            for (String itemId : itemIds) {
                partTaskItems.add(partTaskItemService.get(itemId));
            }
            httpPartTask.setItems(partTaskItems);
        }
        return httpPartTask;
    }

    public void add(HttpPartTaskBean httpPartTaskBean) {
        getHttpPartTaskEntityDao().insert(beanToEntity(httpPartTaskBean));
    }

    public void update(HttpPartTaskBean httpPartTaskBean) {
        getHttpPartTaskEntityDao().update(beanToEntity(httpPartTaskBean));
    }

    private HttpPartTaskEntityDao getHttpPartTaskEntityDao() {
        return mDBManager.getDaoSession().getHttpPartTaskEntityDao();
    }

    private static HttpPartTaskEntity beanToEntity(HttpPartTaskBean httpPartTask) {
        HttpPartTaskEntity httpPartTaskEntity = new HttpPartTaskEntity();
        httpPartTaskEntity.setId(httpPartTask.getId());
        httpPartTaskEntity.setState(httpPartTask.getState());
        httpPartTaskEntity.setCreateTime(httpPartTask.getCreateTime());
        httpPartTaskEntity.setErrorMsg(httpPartTask.getErrorMsg());

        List<HttpPartTaskItemBean> items = httpPartTask.getItems();
        if (items != null && items.size() > 0) {
            StringBuilder itemIdsSB = new StringBuilder();
            for (int i = 0; i < items.size(); i++) {
                HttpPartTaskItemBean item = items.get(i);
                String id = item.getId();
                itemIdsSB.append(id);
                if (i != items.size() - 1) {
                    itemIdsSB.append(",");
                }
            }
            httpPartTaskEntity.setItemIds(itemIdsSB.toString());
        }
        return httpPartTaskEntity;
    }

    public void remove(String id) {
        getHttpPartTaskEntityDao().deleteByKey(id);
    }
}

package com.zhangqiang.downloadmanager.plugin.m3u8.service;

import android.content.Context;

import com.google.gson.Gson;
import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.M3u8TaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.M3u8TaskEntity;
import com.zhangqiang.downloadmanager.plugin.m3u8.bean.M3u8TaskBean;
import com.zhangqiang.downloadmanager.plugin.m3u8.bean.TSTaskBean;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class M3u8TaskService {

    private final DBManager mDBManager;

    private final TSTaskService tsTaskService;
    private final Gson gson = new Gson();

    public M3u8TaskService(Context context) {
        this.mDBManager = new DBManager(context);
        tsTaskService = new TSTaskService(context);
    }

    public List<M3u8TaskBean> getM3u8Tasks() {
        M3u8TaskEntityDao m3u8TaskEntityDao = getM3u8TaskEntityDao();
        List<M3u8TaskEntity> list = m3u8TaskEntityDao.queryBuilder().orderDesc(M3u8TaskEntityDao.Properties.CreateTime).list();

        if (list != null) {
            List<M3u8TaskBean> beans = new ArrayList<>();
            for (M3u8TaskEntity entity : list) {
                M3u8TaskBean bean = new M3u8TaskBean();
                bean.setId(entity.getId());
                bean.setCreateTime(entity.getCreateTime());
                bean.setDuration(entity.getDuration());
                bean.setM3u8FileInfo(gson.fromJson(entity.getM3u8Info(), M3u8File.class));
                bean.setErrorMsg(entity.getErrorMsg());
                bean.setState(entity.getState());
                bean.setSaveDir(entity.getSaveDir());
                bean.setUrl(entity.getUrl());
                bean.setSaveFileName(entity.getSaveFileName());
                bean.setTargetFileName(entity.getTargetFileName());
                String tsIds = entity.getTsIds();
                if (tsIds != null) {
                    List<String> idList = Arrays.asList(tsIds.split(","));
                    bean.setTsTaskBeans(tsTaskService.getTSTaskBeans(idList));
                }
                beans.add(bean);
            }
            return beans;
        }
        return null;
    }

    private M3u8TaskEntityDao getM3u8TaskEntityDao() {
        return mDBManager.getDaoSession().getM3u8TaskEntityDao();
    }

    public void add(M3u8TaskBean bean) {
        getM3u8TaskEntityDao().insert(beanToEntity(bean));
    }

    public void update(M3u8TaskBean bean) {
        getM3u8TaskEntityDao().update(beanToEntity(bean));
    }

    private M3u8TaskEntity beanToEntity(M3u8TaskBean bean) {
        M3u8TaskEntity entity = new M3u8TaskEntity();
        entity.setId(bean.getId());
        entity.setDuration(bean.getDuration());
        entity.setCreateTime(bean.getCreateTime());
        entity.setSaveDir(bean.getSaveDir());
        entity.setErrorMsg(bean.getErrorMsg());
        entity.setState(bean.getState());
        entity.setUrl(bean.getUrl());
        entity.setM3u8Info(gson.toJson(bean.getM3u8FileInfo()));
        entity.setSaveFileName(bean.getSaveFileName());
        entity.setTargetFileName(bean.getTargetFileName());
        List<TSTaskBean> tsTaskBeans = bean.getTsTaskBeans();
        if (tsTaskBeans != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tsTaskBeans.size(); i++) {
                TSTaskBean tsTaskBean = tsTaskBeans.get(i);
                sb.append(tsTaskBean.getId());
                if (i != tsTaskBeans.size() - 1) {
                    sb.append(",");
                }
            }
            entity.setTsIds(sb.toString());
        }
        return entity;
    }

    public void remove(String id) {
        getM3u8TaskEntityDao().deleteByKey(id);
    }
}

package com.zhangqiang.web.history.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.zhangqiang.common.utils.BitmapUtils;
import com.zhangqiang.web.db.DBManager;
import com.zhangqiang.web.db.dao.VisitRecordEntityDao;
import com.zhangqiang.web.db.entity.VisitRecordEntity;
import com.zhangqiang.web.history.bean.VisitRecordBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VisitRecordService {

    private final DBManager dbManager;

    public VisitRecordService(Context context) {
        dbManager = new DBManager(context.getApplicationContext());
    }

    public void save(String url) {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().where(VisitRecordEntityDao.Properties.Url.eq(url)).list();
        if (recordEntities != null && recordEntities.size() > 0) {
            for (VisitRecordEntity recordEntity : recordEntities) {
                recordEntity.setVisitDate(System.currentTimeMillis());
            }
            getVisitRecordEntityDao().updateInTx(recordEntities);
            return;
        }
        VisitRecordEntityDao entityDao = getVisitRecordEntityDao();
        VisitRecordEntity entity = new VisitRecordEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setUrl(url);
        entity.setVisitDate(System.currentTimeMillis());
        entityDao.insert(entity);
    }

    private VisitRecordEntityDao getVisitRecordEntityDao() {
        return dbManager.getDaoSession().getVisitRecordEntityDao();
    }

    public void remove(String id) {
        getVisitRecordEntityDao().deleteByKey(id);
    }

    public List<VisitRecordBean> getVisitRecords() {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().orderDesc(VisitRecordEntityDao.Properties.VisitDate).list();
        if (recordEntities != null) {
            List<VisitRecordBean> visitRecordBeans = new ArrayList<>();
            for (VisitRecordEntity recordEntity : recordEntities) {
                VisitRecordBean visitRecordBean = new VisitRecordBean();
                visitRecordBean.setId(recordEntity.getId());
                visitRecordBean.setUrl(recordEntity.getUrl());
                visitRecordBean.setTitle(recordEntity.getTitle());
                visitRecordBean.setIconUrl(recordEntity.getIconUrl());
                visitRecordBean.setVisitDate(recordEntity.getVisitDate());
                visitRecordBeans.add(visitRecordBean);
            }
            return visitRecordBeans;
        }
        return null;
    }

    public List<VisitRecordBean> getVisitRecords2() {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().orderDesc(VisitRecordEntityDao.Properties.VisitDate).list();
        if (recordEntities != null) {
            List<VisitRecordBean> visitRecordBeans = new ArrayList<>();
            for (VisitRecordEntity recordEntity : recordEntities) {

                visitRecordBeans.add(entityToBean(recordEntity));
            }
            return visitRecordBeans;
        }
        return null;
    }

    private VisitRecordBean entityToBean(VisitRecordEntity recordEntity) {
        VisitRecordBean visitRecordBean = new VisitRecordBean();
        visitRecordBean.setId(recordEntity.getId());
        visitRecordBean.setUrl(recordEntity.getUrl());
        visitRecordBean.setTitle(recordEntity.getTitle());
        visitRecordBean.setIconUrl(recordEntity.getIconUrl());
        visitRecordBean.setVisitDate(recordEntity.getVisitDate());
        return visitRecordBean;
    }

    public void updateTitle(String url, String title) {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().where(VisitRecordEntityDao.Properties.Url.eq(url)).list();
        if (recordEntities != null && recordEntities.size() > 0) {
            for (VisitRecordEntity recordEntity : recordEntities) {
                recordEntity.setTitle(title);
            }
            getVisitRecordEntityDao().updateInTx(recordEntities);
        }
    }

    public void updateIcon(String url, String iconUrl) {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().where(VisitRecordEntityDao.Properties.Url.like("%" + getHost(url) + "%")).list();
        if (recordEntities != null && recordEntities.size() > 0) {
            for (VisitRecordEntity recordEntity : recordEntities) {
                recordEntity.setIconUrl(iconUrl);
            }
            getVisitRecordEntityDao().updateInTx(recordEntities);
        }
    }

    private String getHost(String url) {
        return Uri.parse(url).getHost();
    }

    public VisitRecordBean getLastVisitRecord() {
        VisitRecordEntity entity = getVisitRecordEntityDao().queryBuilder().orderDesc(VisitRecordEntityDao.Properties.VisitDate).limit(1).unique();
        if (entity != null) {
            return entityToBean(entity);
        }
        return null;
    }
}

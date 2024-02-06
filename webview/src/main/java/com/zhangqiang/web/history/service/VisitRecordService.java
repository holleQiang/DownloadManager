package com.zhangqiang.web.history.service;

import android.content.Context;
import android.graphics.Bitmap;

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

    public void add(String url) {
        VisitRecordEntityDao entityDao = getVisitRecordEntityDao();
        VisitRecordEntity recordEntity = entityDao.queryBuilder().where(VisitRecordEntityDao.Properties.Url.eq(url)).unique();
        if (recordEntity != null) {
            return;
        }
        VisitRecordEntity entity = new VisitRecordEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setUrl(url);
        entityDao.insert(entity);
    }

    private VisitRecordEntityDao getVisitRecordEntityDao() {
        return dbManager.getDaoSession().getVisitRecordEntityDao();
    }

    public void remove(String id) {
        getVisitRecordEntityDao().deleteByKey(id);
    }

    public List<VisitRecordBean> getVisitRecords() {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder().list();
        if (recordEntities != null) {
            List<VisitRecordBean> visitRecordBeans = new ArrayList<>();
            for (VisitRecordEntity recordEntity : recordEntities) {
                VisitRecordBean visitRecordBean = new VisitRecordBean();
                visitRecordBean.setId(recordEntity.getId());
                visitRecordBean.setUrl(recordEntity.getUrl());
                visitRecordBean.setTitle(recordEntity.getTitle());
                visitRecordBean.setIcon(BitmapUtils.byteArrayToBitmap(recordEntity.getIcon()));
                visitRecordBeans.add(visitRecordBean);
            }
            return visitRecordBeans;
        }
        return null;
    }

    public void updateTitle(String url, String title) {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder()
                .where(VisitRecordEntityDao.Properties.Url.eq(url))
                .list();
        if (recordEntities != null && recordEntities.size() > 0) {
            for (VisitRecordEntity recordEntity : recordEntities) {
                recordEntity.setTitle(title);
            }
            getVisitRecordEntityDao().updateInTx(recordEntities);
        }
    }

    public void updateIcon(String url, Bitmap bitmap) {
        List<VisitRecordEntity> recordEntities = getVisitRecordEntityDao().queryBuilder()
                .where(VisitRecordEntityDao.Properties.Url.eq(url))
                .list();
        if (recordEntities != null && recordEntities.size() > 0) {
            byte[] iconData = BitmapUtils.bitmapToByteArray(bitmap);
            for (VisitRecordEntity recordEntity : recordEntities) {
                recordEntity.setIcon(iconData);
            }
            getVisitRecordEntityDao().updateInTx(recordEntities);
        }
    }
}

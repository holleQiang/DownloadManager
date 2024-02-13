package com.zhangqiang.web.boomark.service;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhangqiang.web.boomark.bean.BookMarkBean;
import com.zhangqiang.web.db.DBManager;
import com.zhangqiang.web.db.dao.BookMarkEntityDao;
import com.zhangqiang.web.db.entity.BookMarkEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BookMarkService {

    private final DBManager dbManager;

    public BookMarkService(Context context) {
        dbManager = new DBManager(context.getApplicationContext());
    }

    public BookMarkEntityDao getBookMarkEntityDao() {
        return dbManager.getDaoSession().getBookMarkEntityDao();
    }

    public void add(BookMarkBean bookMarkBean, BookMarkBean parent) {
        BookMarkEntity entity = beanToEntity(bookMarkBean, parent);
        getBookMarkEntityDao().insert(entity);
    }

    @NonNull
    private BookMarkEntity beanToEntity(BookMarkBean bookMarkBean, BookMarkBean parent) {
        BookMarkEntity entity = new BookMarkEntity();
        entity.setId(bookMarkBean.getId());
        entity.setTitle(bookMarkBean.getTitle());
        entity.setUrl(bookMarkBean.getUrl());
        if (parent != null) {
            entity.setParentId(parent.getId());
        }
        return entity;
    }

    public List<BookMarkBean> getBookMarks() {
        List<BookMarkEntity> entities = getBookMarkEntityDao().queryBuilder().where(BookMarkEntityDao.Properties.ParentId.isNull()).list();
        if (entities != null) {
            List<BookMarkBean> bookMarkBeans = new ArrayList<>();
            for (BookMarkEntity entity : entities) {
                bookMarkBeans.add(entityToBean(entity));
            }
            return bookMarkBeans;
        }
        return null;
    }

    private BookMarkBean entityToBean(BookMarkEntity entity) {
        BookMarkBean bookMarkBean = new BookMarkBean();
        bookMarkBean.setId(entity.getId());
        bookMarkBean.setTitle(entity.getTitle());
        bookMarkBean.setUrl(entity.getUrl());
        String childIds = entity.getChildIds();
        if (!TextUtils.isEmpty(childIds)) {
            List<String> ids = Arrays.asList(childIds.split(","));
            List<BookMarkBean> children = getBookMarksById(ids);
            if (children != null) {
                for (BookMarkBean child : children) {
                    child.setParent(bookMarkBean);
                }
            }
            bookMarkBean.setChildren(children);
        }
        return bookMarkBean;
    }


    public List<BookMarkBean> getBookMarksById(List<String> ids) {
        List<BookMarkEntity> entities = getBookMarkEntityDao().queryBuilder().where(BookMarkEntityDao.Properties.Id.in(ids)).list();
        if (entities != null) {
            List<BookMarkBean> bookMarkBeans = new ArrayList<>();
            for (BookMarkEntity entity : entities) {
                bookMarkBeans.add(entityToBean(entity));
            }
            return bookMarkBeans;
        }
        return null;
    }

    public boolean isExists(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }
        List<BookMarkEntity> list = getBookMarkEntityDao().queryBuilder().where(BookMarkEntityDao.Properties.Url.eq(url)).list();
        return list != null && !list.isEmpty();
    }

    public void deleteByUrl(String url) {
        List<BookMarkEntity> list = getBookMarkEntityDao().queryBuilder().where(BookMarkEntityDao.Properties.Url.eq(url)).list();
        if (list != null && list.size() > 0) {
            getBookMarkEntityDao().deleteInTx(list);
        }
    }

}

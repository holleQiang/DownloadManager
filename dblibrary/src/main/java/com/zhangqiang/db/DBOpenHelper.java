package com.zhangqiang.db;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.db.dao.BaseDao;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBOpenHelper extends SQLiteOpenHelper {

    private final Map<Class, BaseDao> daoMap = new HashMap<>();
    private final List<Class<? extends BaseDao>> daoClassList = new ArrayList<>();

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @TargetApi(Build.VERSION_CODES.P)
    public DBOpenHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @SuppressWarnings("unchecked")
    public <D extends BaseDao> D getDao(Class<D> daoClass) {

        BaseDao baseDao = daoMap.get(daoClass);
        if (baseDao == null) {
            synchronized (this) {
                baseDao = daoMap.get(daoClass);
                if (baseDao == null) {
                    D dao = createDao(daoClass);
                    daoMap.put(daoClass, dao);
                    return dao;
                }
                return (D) baseDao;
            }
        }
        return (D) baseDao;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (int i = 0; i < daoClassList.size(); i++) {
            Class<? extends BaseDao> daoClass = daoClassList.get(i);
            BaseDao dao = getDao(daoClass);
            String tableCreateSql = dao.getTableCreateSql();
            db.execSQL(tableCreateSql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = 0; i < daoClassList.size(); i++) {
            Class<? extends BaseDao> daoClass = daoClassList.get(i);
            BaseDao dao = getDao(daoClass);
            String tableName = dao.getTableName();
            db.execSQL("drop table if exists " + tableName);
        }
        onCreate(db);
    }

    public synchronized void registerDao(Class<? extends BaseDao> daoClass) {
        if (daoClassList.contains(daoClass)) {
            return;
        }
        daoClassList.add(daoClass);
    }

    private <D extends BaseDao> D createDao(Class<D> daoClass) {

        try {
            Constructor<D> constructor = daoClass.getConstructor(SQLiteOpenHelper.class);
            return constructor.newInstance(this);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

package com.zhangqiang.downloadmanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.zhangqiang.db.DBOpenHelper;
import com.zhangqiang.downloadmanager.db.dao.PartDao;
import com.zhangqiang.downloadmanager.db.dao.TaskDao;

public class DBManager {

    private static final String DB_NAME = "download_manager.db";
    private static final int DB_VERSION = 4;
    private final DBOpenHelper dbOpenHelper;
    private static volatile DBManager instance;

    public static DBManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("init method must be called");
        }
        return instance;
    }

    private DBManager(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        dbOpenHelper = new DBOpenHelper(context, name, factory, version);
        dbOpenHelper.registerDao(TaskDao.class);
        dbOpenHelper.registerDao(PartDao.class);
    }

    public static void init(Context context) {

        if (instance != null) {
            return;
        }
        synchronized (DBManager.class) {
            if (instance == null) {
                instance = new DBManager(context, DB_NAME, null, DB_VERSION);
            }
        }
    }


    public TaskDao getTaskDao() {
        return dbOpenHelper.getDao(TaskDao.class);
    }

    public PartDao getPartDao() {
        return dbOpenHelper.getDao(PartDao.class);
    }

}

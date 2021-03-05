package com.zhangqiang.downloadmanager.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.zhangqiang.downloadmanager.db.dao.DaoMaster;
import com.zhangqiang.downloadmanager.db.dao.DaoSession;

public class DBManager {

    private static final String DB_NAME = "download_manager.db";
    private static volatile DBManager instance;
    private final ThreadLocal<DaoSession> mDaoSessionRef = new ThreadLocal<DaoSession>(){
        @Nullable
        @Override
        protected DaoSession initialValue() {
            return mDaoMaster.newSession();
        }
    };
    private final DaoMaster mDaoMaster;

    public static DBManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DBManager.class){
                if (instance == null) {
                    instance = new DBManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DBManager(@Nullable Context context) {
        DaoMaster.DevOpenHelper openHelper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(database);
    }

    public DaoSession getDaoSession(){
        return mDaoSessionRef.get();
    }
}

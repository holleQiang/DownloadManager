package com.zhangqiang.myftp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.zhangqiang.myftp.db.dao.AccountEntityDao;
import com.zhangqiang.myftp.db.dao.DaoMaster;
import com.zhangqiang.myftp.db.dao.DaoSession;

import org.greenrobot.greendao.database.Database;

public class DBService {

    private static final String DB_NAME = "my_ftp.db";
    private final ThreadLocal<DaoSession> mDaoSessionRef = new ThreadLocal<DaoSession>(){
        @Nullable
        @Override
        protected DaoSession initialValue() {
            return mDaoMaster.newSession();
        }
    };
    private final DaoMaster mDaoMaster;

    public DBService(@Nullable Context context) {
        DaoMaster.OpenHelper openHelper = new DBService.MyDBOpenHelper(context, DB_NAME, null);
        SQLiteDatabase database = openHelper.getWritableDatabase();
        mDaoMaster = new DaoMaster(database);
    }

    public DaoSession getDaoSession(){
        return mDaoSessionRef.get();
    }

    private static class MyDBOpenHelper extends DaoMaster.OpenHelper{

        public MyDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {
                        @Override
                        public void onCreateAllTables(Database db, boolean ifNotExists) {
                            DaoMaster.createAllTables(db, ifNotExists);
                        }
                        @Override
                        public void onDropAllTables(Database db, boolean ifExists) {
                            DaoMaster.dropAllTables(db, ifExists);
                        }
                    },
                    AccountEntityDao.class);
        }
    }
}

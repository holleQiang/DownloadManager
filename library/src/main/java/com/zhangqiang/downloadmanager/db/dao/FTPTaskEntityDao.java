package com.zhangqiang.downloadmanager.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zhangqiang.downloadmanager.db.entity.FTPTaskEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "FTPTASK_ENTITY".
 */
public class FTPTaskEntityDao extends AbstractDao<FTPTaskEntity, String> {

    public static final String TABLENAME = "FTPTASK_ENTITY";

    /**
     * Properties of entity FTPTaskEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property Host = new Property(1, String.class, "host", false, "HOST");
        public final static Property Port = new Property(2, int.class, "port", false, "PORT");
        public final static Property UserName = new Property(3, String.class, "userName", false, "USER_NAME");
        public final static Property Password = new Property(4, String.class, "password", false, "PASSWORD");
        public final static Property FtpDir = new Property(5, String.class, "ftpDir", false, "FTP_DIR");
        public final static Property FtpFileName = new Property(6, String.class, "ftpFileName", false, "FTP_FILE_NAME");
        public final static Property SaveDir = new Property(7, String.class, "saveDir", false, "SAVE_DIR");
        public final static Property SaveFileName = new Property(8, String.class, "saveFileName", false, "SAVE_FILE_NAME");
        public final static Property TargetFileName = new Property(9, String.class, "targetFileName", false, "TARGET_FILE_NAME");
        public final static Property CurrentLength = new Property(10, long.class, "currentLength", false, "CURRENT_LENGTH");
        public final static Property ContentType = new Property(11, String.class, "contentType", false, "CONTENT_TYPE");
        public final static Property ContentLength = new Property(12, long.class, "contentLength", false, "CONTENT_LENGTH");
        public final static Property State = new Property(13, int.class, "state", false, "STATE");
        public final static Property ErrorMsg = new Property(14, String.class, "errorMsg", false, "ERROR_MSG");
        public final static Property CreateTime = new Property(15, long.class, "createTime", false, "CREATE_TIME");
    }


    public FTPTaskEntityDao(DaoConfig config) {
        super(config);
    }

    public FTPTaskEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"FTPTASK_ENTITY\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"HOST\" TEXT," + // 1: host
                "\"PORT\" INTEGER NOT NULL ," + // 2: port
                "\"USER_NAME\" TEXT," + // 3: userName
                "\"PASSWORD\" TEXT," + // 4: password
                "\"FTP_DIR\" TEXT," + // 5: ftpDir
                "\"FTP_FILE_NAME\" TEXT," + // 6: ftpFileName
                "\"SAVE_DIR\" TEXT," + // 7: saveDir
                "\"SAVE_FILE_NAME\" TEXT," + // 8: saveFileName
                "\"TARGET_FILE_NAME\" TEXT," + // 9: targetFileName
                "\"CURRENT_LENGTH\" INTEGER NOT NULL ," + // 10: currentLength
                "\"CONTENT_TYPE\" TEXT," + // 11: contentType
                "\"CONTENT_LENGTH\" INTEGER NOT NULL ," + // 12: contentLength
                "\"STATE\" INTEGER NOT NULL ," + // 13: state
                "\"ERROR_MSG\" TEXT," + // 14: errorMsg
                "\"CREATE_TIME\" INTEGER NOT NULL );"); // 15: createTime
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"FTPTASK_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, FTPTaskEntity entity) {
        stmt.clearBindings();

        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }

        String host = entity.getHost();
        if (host != null) {
            stmt.bindString(2, host);
        }
        stmt.bindLong(3, entity.getPort());

        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(4, userName);
        }

        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(5, password);
        }

        String ftpDir = entity.getFtpDir();
        if (ftpDir != null) {
            stmt.bindString(6, ftpDir);
        }

        String ftpFileName = entity.getFtpFileName();
        if (ftpFileName != null) {
            stmt.bindString(7, ftpFileName);
        }

        String saveDir = entity.getSaveDir();
        if (saveDir != null) {
            stmt.bindString(8, saveDir);
        }

        String saveFileName = entity.getSaveFileName();
        if (saveFileName != null) {
            stmt.bindString(9, saveFileName);
        }

        String targetFileName = entity.getTargetFileName();
        if (targetFileName != null) {
            stmt.bindString(10, targetFileName);
        }
        stmt.bindLong(11, entity.getCurrentLength());

        String contentType = entity.getContentType();
        if (contentType != null) {
            stmt.bindString(12, contentType);
        }
        stmt.bindLong(13, entity.getContentLength());
        stmt.bindLong(14, entity.getState());

        String errorMsg = entity.getErrorMsg();
        if (errorMsg != null) {
            stmt.bindString(15, errorMsg);
        }
        stmt.bindLong(16, entity.getCreateTime());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, FTPTaskEntity entity) {
        stmt.clearBindings();

        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }

        String host = entity.getHost();
        if (host != null) {
            stmt.bindString(2, host);
        }
        stmt.bindLong(3, entity.getPort());

        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(4, userName);
        }

        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(5, password);
        }

        String ftpDir = entity.getFtpDir();
        if (ftpDir != null) {
            stmt.bindString(6, ftpDir);
        }

        String ftpFileName = entity.getFtpFileName();
        if (ftpFileName != null) {
            stmt.bindString(7, ftpFileName);
        }

        String saveDir = entity.getSaveDir();
        if (saveDir != null) {
            stmt.bindString(8, saveDir);
        }

        String saveFileName = entity.getSaveFileName();
        if (saveFileName != null) {
            stmt.bindString(9, saveFileName);
        }

        String targetFileName = entity.getTargetFileName();
        if (targetFileName != null) {
            stmt.bindString(10, targetFileName);
        }
        stmt.bindLong(11, entity.getCurrentLength());

        String contentType = entity.getContentType();
        if (contentType != null) {
            stmt.bindString(12, contentType);
        }
        stmt.bindLong(13, entity.getContentLength());
        stmt.bindLong(14, entity.getState());

        String errorMsg = entity.getErrorMsg();
        if (errorMsg != null) {
            stmt.bindString(15, errorMsg);
        }
        stmt.bindLong(16, entity.getCreateTime());
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }

    @Override
    public FTPTaskEntity readEntity(Cursor cursor, int offset) {
        FTPTaskEntity entity = new FTPTaskEntity( //
                cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // host
                cursor.getInt(offset + 2), // port
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // userName
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // password
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // ftpDir
                cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // ftpFileName
                cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7), // saveDir
                cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // saveFileName
                cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // targetFileName
                cursor.getLong(offset + 10), // currentLength
                cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11), // contentType
                cursor.getLong(offset + 12), // contentLength
                cursor.getInt(offset + 13), // state
                cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14), // errorMsg
                cursor.getLong(offset + 15) // createTime
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, FTPTaskEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setHost(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setPort(cursor.getInt(offset + 2));
        entity.setUserName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setPassword(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setFtpDir(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setFtpFileName(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setSaveDir(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
        entity.setSaveFileName(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setTargetFileName(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setCurrentLength(cursor.getLong(offset + 10));
        entity.setContentType(cursor.isNull(offset + 11) ? null : cursor.getString(offset + 11));
        entity.setContentLength(cursor.getLong(offset + 12));
        entity.setState(cursor.getInt(offset + 13));
        entity.setErrorMsg(cursor.isNull(offset + 14) ? null : cursor.getString(offset + 14));
        entity.setCreateTime(cursor.getLong(offset + 15));
    }

    @Override
    protected final String updateKeyAfterInsert(FTPTaskEntity entity, long rowId) {
        return entity.getId();
    }

    @Override
    public String getKey(FTPTaskEntity entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(FTPTaskEntity entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

}

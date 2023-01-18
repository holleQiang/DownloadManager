package com.zhangqiang.downloadmanager.db.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.zhangqiang.downloadmanager.db.entity.HttpPartTaskItemEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "HTTP_PART_TASK_ITEM_ENTITY".
*/
public class HttpPartTaskItemEntityDao extends AbstractDao<HttpPartTaskItemEntity, String> {

    public static final String TABLENAME = "HTTP_PART_TASK_ITEM_ENTITY";

    /**
     * Properties of entity HttpPartTaskItemEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, String.class, "id", true, "ID");
        public final static Property FilePath = new Property(1, String.class, "filePath", false, "FILE_PATH");
        public final static Property StartPosition = new Property(2, long.class, "startPosition", false, "START_POSITION");
        public final static Property CurrentPosition = new Property(3, long.class, "currentPosition", false, "CURRENT_POSITION");
        public final static Property EndPosition = new Property(4, long.class, "endPosition", false, "END_POSITION");
        public final static Property State = new Property(5, int.class, "state", false, "STATE");
        public final static Property CreateTime = new Property(6, java.util.Date.class, "createTime", false, "CREATE_TIME");
        public final static Property ErrorMsg = new Property(7, String.class, "errorMsg", false, "ERROR_MSG");
    }


    public HttpPartTaskItemEntityDao(DaoConfig config) {
        super(config);
    }
    
    public HttpPartTaskItemEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"HTTP_PART_TASK_ITEM_ENTITY\" (" + //
                "\"ID\" TEXT PRIMARY KEY NOT NULL ," + // 0: id
                "\"FILE_PATH\" TEXT NOT NULL ," + // 1: filePath
                "\"START_POSITION\" INTEGER NOT NULL ," + // 2: startPosition
                "\"CURRENT_POSITION\" INTEGER NOT NULL ," + // 3: currentPosition
                "\"END_POSITION\" INTEGER NOT NULL ," + // 4: endPosition
                "\"STATE\" INTEGER NOT NULL ," + // 5: state
                "\"CREATE_TIME\" INTEGER NOT NULL ," + // 6: createTime
                "\"ERROR_MSG\" TEXT);"); // 7: errorMsg
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"HTTP_PART_TASK_ITEM_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, HttpPartTaskItemEntity entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
        stmt.bindString(2, entity.getFilePath());
        stmt.bindLong(3, entity.getStartPosition());
        stmt.bindLong(4, entity.getCurrentPosition());
        stmt.bindLong(5, entity.getEndPosition());
        stmt.bindLong(6, entity.getState());
        stmt.bindLong(7, entity.getCreateTime().getTime());
 
        String errorMsg = entity.getErrorMsg();
        if (errorMsg != null) {
            stmt.bindString(8, errorMsg);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, HttpPartTaskItemEntity entity) {
        stmt.clearBindings();
 
        String id = entity.getId();
        if (id != null) {
            stmt.bindString(1, id);
        }
        stmt.bindString(2, entity.getFilePath());
        stmt.bindLong(3, entity.getStartPosition());
        stmt.bindLong(4, entity.getCurrentPosition());
        stmt.bindLong(5, entity.getEndPosition());
        stmt.bindLong(6, entity.getState());
        stmt.bindLong(7, entity.getCreateTime().getTime());
 
        String errorMsg = entity.getErrorMsg();
        if (errorMsg != null) {
            stmt.bindString(8, errorMsg);
        }
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0);
    }    

    @Override
    public HttpPartTaskItemEntity readEntity(Cursor cursor, int offset) {
        HttpPartTaskItemEntity entity = new HttpPartTaskItemEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0), // id
            cursor.getString(offset + 1), // filePath
            cursor.getLong(offset + 2), // startPosition
            cursor.getLong(offset + 3), // currentPosition
            cursor.getLong(offset + 4), // endPosition
            cursor.getInt(offset + 5), // state
            new java.util.Date(cursor.getLong(offset + 6)), // createTime
            cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7) // errorMsg
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, HttpPartTaskItemEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getString(offset + 0));
        entity.setFilePath(cursor.getString(offset + 1));
        entity.setStartPosition(cursor.getLong(offset + 2));
        entity.setCurrentPosition(cursor.getLong(offset + 3));
        entity.setEndPosition(cursor.getLong(offset + 4));
        entity.setState(cursor.getInt(offset + 5));
        entity.setCreateTime(new java.util.Date(cursor.getLong(offset + 6)));
        entity.setErrorMsg(cursor.isNull(offset + 7) ? null : cursor.getString(offset + 7));
     }
    
    @Override
    protected final String updateKeyAfterInsert(HttpPartTaskItemEntity entity, long rowId) {
        return entity.getId();
    }
    
    @Override
    public String getKey(HttpPartTaskItemEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(HttpPartTaskItemEntity entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}

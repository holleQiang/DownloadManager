package com.zhangqiang.downloadmanager.db.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.zhangqiang.downloadmanager.db.entity.PartEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "PART_ENTITY".
*/
public class PartEntityDao extends AbstractDao<PartEntity, Long> {

    public static final String TABLENAME = "PART_ENTITY";

    /**
     * Properties of entity PartEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TaskId = new Property(1, Long.class, "taskId", false, "TASK_ID");
        public final static Property SavePath = new Property(2, String.class, "savePath", false, "SAVE_PATH");
        public final static Property Current = new Property(3, long.class, "current", false, "CURRENT");
        public final static Property Start = new Property(4, long.class, "start", false, "START");
        public final static Property End = new Property(5, long.class, "end", false, "END");
        public final static Property ThreadIndex = new Property(6, int.class, "threadIndex", false, "THREAD_INDEX");
        public final static Property ThreadSize = new Property(7, int.class, "threadSize", false, "THREAD_SIZE");
    }

    private Query<PartEntity> taskEntity_PartListQuery;

    public PartEntityDao(DaoConfig config) {
        super(config);
    }
    
    public PartEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"PART_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TASK_ID\" INTEGER," + // 1: taskId
                "\"SAVE_PATH\" TEXT," + // 2: savePath
                "\"CURRENT\" INTEGER NOT NULL ," + // 3: current
                "\"START\" INTEGER NOT NULL ," + // 4: start
                "\"END\" INTEGER NOT NULL ," + // 5: end
                "\"THREAD_INDEX\" INTEGER NOT NULL ," + // 6: threadIndex
                "\"THREAD_SIZE\" INTEGER NOT NULL );"); // 7: threadSize
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"PART_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, PartEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long taskId = entity.getTaskId();
        if (taskId != null) {
            stmt.bindLong(2, taskId);
        }
 
        String savePath = entity.getSavePath();
        if (savePath != null) {
            stmt.bindString(3, savePath);
        }
        stmt.bindLong(4, entity.getCurrent());
        stmt.bindLong(5, entity.getStart());
        stmt.bindLong(6, entity.getEnd());
        stmt.bindLong(7, entity.getThreadIndex());
        stmt.bindLong(8, entity.getThreadSize());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, PartEntity entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Long taskId = entity.getTaskId();
        if (taskId != null) {
            stmt.bindLong(2, taskId);
        }
 
        String savePath = entity.getSavePath();
        if (savePath != null) {
            stmt.bindString(3, savePath);
        }
        stmt.bindLong(4, entity.getCurrent());
        stmt.bindLong(5, entity.getStart());
        stmt.bindLong(6, entity.getEnd());
        stmt.bindLong(7, entity.getThreadIndex());
        stmt.bindLong(8, entity.getThreadSize());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public PartEntity readEntity(Cursor cursor, int offset) {
        PartEntity entity = new PartEntity( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1), // taskId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // savePath
            cursor.getLong(offset + 3), // current
            cursor.getLong(offset + 4), // start
            cursor.getLong(offset + 5), // end
            cursor.getInt(offset + 6), // threadIndex
            cursor.getInt(offset + 7) // threadSize
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, PartEntity entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTaskId(cursor.isNull(offset + 1) ? null : cursor.getLong(offset + 1));
        entity.setSavePath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setCurrent(cursor.getLong(offset + 3));
        entity.setStart(cursor.getLong(offset + 4));
        entity.setEnd(cursor.getLong(offset + 5));
        entity.setThreadIndex(cursor.getInt(offset + 6));
        entity.setThreadSize(cursor.getInt(offset + 7));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(PartEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(PartEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(PartEntity entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "partList" to-many relationship of TaskEntity. */
    public List<PartEntity> _queryTaskEntity_PartList(Long taskId) {
        synchronized (this) {
            if (taskEntity_PartListQuery == null) {
                QueryBuilder<PartEntity> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.TaskId.eq(null));
                taskEntity_PartListQuery = queryBuilder.build();
            }
        }
        Query<PartEntity> query = taskEntity_PartListQuery.forCurrentThread();
        query.setParameter(0, taskId);
        return query.list();
    }

}

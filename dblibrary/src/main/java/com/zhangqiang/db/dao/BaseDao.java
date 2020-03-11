package com.zhangqiang.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.db.entity.DBEntity;
import com.zhangqiang.db.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDao<E extends DBEntity> {

    private static final String TABLE_NAME_PREFIX = "tb_";
    private static final String COLUMN_UNIQUE_ID = "unique_id";
    private SQLiteOpenHelper sqLiteOpenHelper;
    private String mTableName;

    public BaseDao(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        this.sqLiteOpenHelper = sqLiteOpenHelper;
    }

    @NonNull
    protected abstract String onCreateTableName();

    @NonNull
    protected abstract List<ColumnEntry> getColumnEntries();

    @NonNull
    protected abstract ContentValues toContentValues(E entity);

    @NonNull
    protected abstract E toDBEntity(Cursor cursor);

    @Nullable
    private SQLiteDatabase getWriteDatabase() {
        try {

            return sqLiteOpenHelper.getWritableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private SQLiteDatabase getReadDatabase() {
        try {

            return sqLiteOpenHelper.getReadableDatabase();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(E entity) {

        if (exists(entity)) {
            return update(entity);
        } else {
            return insert(entity) != -1;
        }
    }

    public boolean delete(E entity) {

        String uniqueId = checkUniqueId(entity);

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return false;
        }

        String tableName = getTableName();
        int effectRows = database.delete(tableName, COLUMN_UNIQUE_ID + "=?", new String[]{uniqueId});
        return effectRows > 0;
    }

    public boolean update(E entity) {

        String uniqueId = checkUniqueId(entity);

        return update(entity,COLUMN_UNIQUE_ID + "=?", new String[]{uniqueId}) > 0;
    }

    public int update(E entity, String whereClause, String[] args) {

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return 0;
        }

        ContentValues contentValues = toContentValuesInternal(entity);

        String tableName = getTableName();

        return database.update(tableName, contentValues, whereClause, args);
    }

    @NonNull
    private ContentValues toContentValuesInternal(E entity) {

        ContentValues contentValues = toContentValues(entity);
        contentValues.put(COLUMN_UNIQUE_ID, entity.getUniqueId());
        return contentValues;
    }


    public long insert(E entity) {

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return -1;
        }

        String tableName = getTableName();

        ContentValues contentValues = toContentValuesInternal(entity);

        return database.insert(tableName, null, contentValues);
    }

    public E queryByUniqueId(String uniqueId) {

        List<E> list = query(null, COLUMN_UNIQUE_ID + "=?", new String[]{uniqueId}, null, null, null, null);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public List<E> queryAll() {

        return query(null, null, null, null, null, null, null);
    }

    public List<E> query(String[] columns, String selection, String[] args, String groupBy,
                         String having, String orderBy, String limit) {

        SQLiteDatabase database = getReadDatabase();
        if (database == null) {
            return null;
        }
        String tableName = getTableName();
        Cursor cursor = null;
        try {

            cursor = database.query(tableName, columns, selection, args, groupBy, having, orderBy, limit);
            List<E> entityList = null;
            while (cursor.moveToNext()) {

                E dbEntity = toDBEntity(cursor);
                if (entityList == null) {
                    entityList = new ArrayList<>();
                }
                entityList.add(dbEntity);
            }
            return entityList;
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }


    public String getTableName() {
        if (mTableName == null) {
            String tableName = onCreateTableName();
            if (StringUtils.isEmpty(tableName)) {
                throw new IllegalArgumentException("tableName cannot be null");
            }
            mTableName = TABLE_NAME_PREFIX + tableName;
        }
        return mTableName;
    }

    private static <E extends DBEntity> String checkUniqueId(E entity) {

        if (entity == null) {
            throw new NullPointerException("entity cannot be null");
        }
        String uniqueId = entity.getUniqueId();
        if (StringUtils.isEmpty(uniqueId)) {
            throw new IllegalArgumentException("uniqueId of entity:" + entity + " cannot be null");
        }
        return uniqueId;
    }

    public boolean exists(E entity) {

        String uniqueId = checkUniqueId(entity);

        SQLiteDatabase database = getWriteDatabase();
        if (database == null) {
            return false;
        }
        String tableName = getTableName();
        Cursor cursor = null;
        try {

            cursor = database.query(tableName,
                    new String[]{COLUMN_UNIQUE_ID},
                    COLUMN_UNIQUE_ID + "= ?",
                    new String[]{uniqueId},
                    null,
                    null,
                    null);
            if (cursor == null) {
                return false;
            }
            if (cursor.moveToNext()) {
                return true;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    public String getTableCreateSql() {

        StringBuilder sqlBuilder = new StringBuilder("create table if not exists " + getTableName() + "(");

        List<ColumnEntry> columnEntries = getColumnEntries();

        columnEntries.addAll(getCommonColumnEntries());

        int entryCount = columnEntries.size();
        for (int i = 0; i < entryCount; i++) {
            ColumnEntry entry = columnEntries.get(i);
            sqlBuilder.append(entry.getName()).append(" ")
                    .append(entry.getType()).append(" ");
            if (entry.isPrimaryKey()) {
                sqlBuilder.append("primary key ");
            } else if (entry.isUnique()) {
                sqlBuilder.append("unique ");
            } else if (entry.isIndex()) {
                sqlBuilder.append("index ");
            }
            if (entry.isAutoIncrement()) {
                sqlBuilder.append("autoincrement ");
            }
            if (i != entryCount - 1) {
                sqlBuilder.append(",");
            }
        }

        sqlBuilder.append(");");
        return sqlBuilder.toString();
    }

    private List<ColumnEntry> getCommonColumnEntries() {

        List<ColumnEntry> columnEntries = new ArrayList<>();

        ColumnEntry uniqueIdEntry = new ColumnEntry();
        uniqueIdEntry.setUnique(true);
        uniqueIdEntry.setName(COLUMN_UNIQUE_ID);
        uniqueIdEntry.setType("TEXT");
        columnEntries.add(uniqueIdEntry);

        return columnEntries;
    }

    public E queryByRowId(long rowId) {

        List<E> list = query(null,
                "rowid = ?", new String[]{rowId + ""},
                null,
                null,
                null,
                null);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

}

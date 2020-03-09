package com.zhangqiang.downloadmanager.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.zhangqiang.db.dao.BaseDao;
import com.zhangqiang.db.dao.ColumnEntry;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;

import java.util.ArrayList;
import java.util.List;

public class PartDao extends BaseDao<PartEntity> {

    private static final String COLUMN_URL = "url";
    private static final String COLUMN_SAVE_PATH = "save_path";
    private static final String COLUMN_CURRENT = "current";
    private static final String COLUMN_START = "start";
    private static final String COLUMN_END = "end";
    private static final String COLUMN_STATUS = "status";

    public PartDao(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper);
    }

    @NonNull
    @Override
    protected String onCreateTableName() {
        return "part_task";
    }

    @NonNull
    @Override
    protected List<ColumnEntry> getColumnEntries() {
        ArrayList<ColumnEntry> columnEntries = new ArrayList<>();
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_URL)
                .setType("TEXT"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_SAVE_PATH)
                .setType("TEXT"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_CURRENT)
                .setType("INTEGER"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_START)
                .setType("INTEGER"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_END)
                .setType("INTEGER"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_STATUS)
                .setType("INTEGER"));
        return columnEntries;
    }

    @NonNull
    @Override
    protected ContentValues toContentValues(PartEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_URL, entity.getUrl());
        contentValues.put(COLUMN_SAVE_PATH, entity.getSavePath());
        contentValues.put(COLUMN_CURRENT, entity.getCurrent());
        contentValues.put(COLUMN_START, entity.getStart());
        contentValues.put(COLUMN_END, entity.getEnd());
        contentValues.put(COLUMN_STATUS, entity.getStatus());
        return contentValues;
    }

    @NonNull
    @Override
    protected PartEntity toDBEntity(Cursor cursor) {
        PartEntity partEntity = new PartEntity();
        partEntity.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
        partEntity.setSavePath(cursor.getString(cursor.getColumnIndex(COLUMN_SAVE_PATH)));
        partEntity.setCurrent(cursor.getLong(cursor.getColumnIndex(COLUMN_CURRENT)));
        partEntity.setStart(cursor.getLong(cursor.getColumnIndex(COLUMN_START)));
        partEntity.setEnd(cursor.getLong(cursor.getColumnIndex(COLUMN_END)));
        partEntity.setStatus(cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS)));
        return partEntity;
    }

    public void resume(PartEntity partEntity) {
        PartEntity entity = queryByUniqueId(partEntity.getUniqueId());
        if (entity == null) {
            insert(partEntity);
            return;
        }
        partEntity.setUrl(entity.getUrl());
        partEntity.setSavePath(entity.getSavePath());
        partEntity.setCurrent(entity.getCurrent());
        partEntity.setStart(entity.getStart());
        partEntity.setEnd(entity.getEnd());
        partEntity.setStatus(entity.getStatus());
    }
}

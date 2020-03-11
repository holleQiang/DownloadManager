package com.zhangqiang.downloadmanager.db.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import com.zhangqiang.db.dao.BaseDao;
import com.zhangqiang.db.dao.ColumnEntry;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;

import java.util.ArrayList;
import java.util.List;

public class TaskDao extends BaseDao<TaskEntity> {

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_SAVE_DIR = "save_dir";
    private static final String COLUMN_FILE_NAME = "file_name";
    private static final String COLUMN_CURRENT_LENGTH = "current_length";
    private static final String COLUMN_TOTAL_LENGTH = "total_length";
    private static final String COLUMN_STATE = "state";
    private static final String COLUMN_E_TAG = "e_tag";
    private static final String COLUMN_LAST_MODIFIED = "last_modified";
    private static final String COLUMN_CONTENT_TYPE = "content_type";
    private static final String COLUMN_CREATE_TIME = "create_time";

    public TaskDao(@NonNull SQLiteOpenHelper sqLiteOpenHelper) {
        super(sqLiteOpenHelper);
    }

    @NonNull
    @Override
    protected String onCreateTableName() {
        return "task";
    }

    @NonNull
    @Override
    protected List<ColumnEntry> getColumnEntries() {
        ArrayList<ColumnEntry> columnEntries = new ArrayList<>();
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_ID)
                .setType("integer")
                .setPrimaryKey(true)
                .setAutoIncrement(true));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_URL)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_SAVE_DIR)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_FILE_NAME)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_CURRENT_LENGTH)
                .setType("integer"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_TOTAL_LENGTH)
                .setType("integer"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_STATE)
                .setType("integer"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_E_TAG)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_LAST_MODIFIED)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_CONTENT_TYPE)
                .setType("text"));
        columnEntries.add(new ColumnEntry()
                .setName(COLUMN_CREATE_TIME)
                .setType("integer"));
        return columnEntries;
    }

    @NonNull
    @Override
    protected ContentValues toContentValues(TaskEntity entity) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID, entity.getId());
        contentValues.put(COLUMN_URL, entity.getUrl());
        contentValues.put(COLUMN_CURRENT_LENGTH, entity.getCurrentLength());
        contentValues.put(COLUMN_SAVE_DIR, entity.getSaveDir());
        contentValues.put(COLUMN_FILE_NAME, entity.getFileName());
        contentValues.put(COLUMN_TOTAL_LENGTH, entity.getTotalLength());
        contentValues.put(COLUMN_STATE, entity.getState());
        contentValues.put(COLUMN_E_TAG, entity.getETag());
        contentValues.put(COLUMN_LAST_MODIFIED, entity.getLastModified());
        contentValues.put(COLUMN_CONTENT_TYPE, entity.getContentType());
        contentValues.put(COLUMN_CREATE_TIME, entity.getCreateTime());
        return contentValues;
    }

    @NonNull
    @Override
    protected TaskEntity toDBEntity(Cursor cursor) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(cursor.getLong(cursor.getColumnIndex(COLUMN_ID)));
        taskEntity.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
        taskEntity.setSaveDir(cursor.getString(cursor.getColumnIndex(COLUMN_SAVE_DIR)));
        taskEntity.setFileName(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_NAME)));
        taskEntity.setCurrentLength(cursor.getLong(cursor.getColumnIndex(COLUMN_CURRENT_LENGTH)));
        taskEntity.setTotalLength(cursor.getLong(cursor.getColumnIndex(COLUMN_TOTAL_LENGTH)));
        taskEntity.setState(cursor.getInt(cursor.getColumnIndex(COLUMN_STATE)));
        taskEntity.setETag(cursor.getString(cursor.getColumnIndex(COLUMN_E_TAG)));
        taskEntity.setLastModified(cursor.getString(cursor.getColumnIndex(COLUMN_LAST_MODIFIED)));
        taskEntity.setContentType(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_TYPE)));
        taskEntity.setCreateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_CREATE_TIME)));
        return taskEntity;
    }

    public void addNewTask(TaskEntity taskEntity) {

        long rowId = insert(taskEntity);
        TaskEntity dbEntity = queryByRowId(rowId);
        if (dbEntity == null) {
            return;
        }
        taskEntity.setId(dbEntity.getId());
        taskEntity.setUrl(dbEntity.getUrl());
        taskEntity.setTotalLength(dbEntity.getTotalLength());
        taskEntity.setCurrentLength(dbEntity.getCurrentLength());
        taskEntity.setSaveDir(dbEntity.getSaveDir());
        taskEntity.setFileName(dbEntity.getFileName());
        taskEntity.setState(dbEntity.getState());
        taskEntity.setETag(dbEntity.getETag());
        taskEntity.setLastModified(dbEntity.getLastModified());
        taskEntity.setContentType(dbEntity.getContentType());
        taskEntity.setCreateTime(dbEntity.getCreateTime());
        update(taskEntity, COLUMN_ID + " = ?", new String[]{taskEntity.getId() + ""});
    }


    public boolean isFileExists(String saveDir, String fileName) {
        List<TaskEntity> list = query(new String[]{COLUMN_ID},
                COLUMN_SAVE_DIR + " = ? and " + COLUMN_FILE_NAME + " = ?",
                new String[]{saveDir, fileName},
                null,
                null,
                null,
                null);
        return list != null && list.size() > 0;
    }
}

package com.zhangqiang.downloadmanager.task;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.helper.ProgressUpdateHelper;
import com.zhangqiang.downloadmanager.utils.MD5Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public abstract class DownloadTask {

    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_COMPLETE = 4;
    private final List<DownloadListener> downloadListeners = new ArrayList<>();
    private final TaskEntity taskEntity;
    private final ProgressUpdateHelper progressUpdateHelper = new ProgressUpdateHelper() {
        @Override
        protected void doUpdate() {
            DBManager.getInstance().getTaskDao().update(taskEntity);
        }
    };

    public DownloadTask(String url, String saveDir) {
        taskEntity = new TaskEntity()
                .setUrl(url)
                .setSaveDir(saveDir)
                .setFileName(getFileName(url))
                .setCreateTime(System.currentTimeMillis());
        DBManager.getInstance().getTaskDao().addNewTask(taskEntity);
    }

    public DownloadTask(TaskEntity taskEntity) {
        this.taskEntity = taskEntity;
    }

    protected abstract void onStart();

    protected abstract void onPause();

    public final void start() {

        int state = getState();
        if (state == STATE_COMPLETE || state == STATE_DOWNLOADING) {
            return;
        }
        setState(STATE_DOWNLOADING);
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onStart();
        }
        onStart();
    }

    public final void pause() {

        int state = getState();
        if (state != STATE_DOWNLOADING) {
            return;
        }
        setState(STATE_PAUSE);
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onPause();
        }
        onPause();
    }


    public String getUrl() {
        return taskEntity.getUrl();
    }

    public String getSaveDir() {
        return taskEntity.getSaveDir();
    }

    public void addDownloadListener(DownloadListener downloadListener) {
        if (downloadListeners.contains(downloadListener)) {
            return;
        }
        downloadListeners.add(downloadListener);
    }

    public void removeDownloadListener(DownloadListener downloadListener) {
        downloadListeners.remove(downloadListener);
    }

    protected void setTotalLength(long total) {
        taskEntity.setTotalLength(total);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    protected void notifyProgress(long current) {
        taskEntity.setCurrentLength(current);
        progressUpdateHelper.update();
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onProgress(current, getTotalLength());
        }
    }

    protected void notifyComplete() {
        setState(STATE_COMPLETE);
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onComplete();
        }
    }

    protected void notifyFail(Throwable e) {
        setStateFail(e.getMessage());
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onFail(e);
        }
    }

    @State
    public int getState() {
        return taskEntity.getState();
    }

    public String getFileName() {
        return taskEntity.getFileName();
    }

    protected void setFileName(String fileName) {

        taskEntity.setFileName(fileName);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    public long getCurrentLength() {
        return taskEntity.getCurrentLength();
    }

    public long getTotalLength() {
        return taskEntity.getTotalLength();
    }

    private static String getFileName(String url) {

        String fileName = getFileNameFromUrl(url);
        if (!TextUtils.isEmpty(fileName)) {
            return fileName;
        }
        return MD5Utils.getMD5(url);
    }

    private static String getFileNameFromUrl(String url) {

        Uri uri = Uri.parse(url);
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment != null && !TextUtils.isEmpty(lastPathSegment)) {
            if (lastPathSegment.contains(".")) {
                return lastPathSegment;
            }
        }
        Set<String> parameterNames = uri.getQueryParameterNames();
        if (parameterNames != null && !parameterNames.isEmpty()) {

            for (String parameterName : parameterNames) {
                String parameter = uri.getQueryParameter(parameterName);
                if (!TextUtils.isEmpty(parameter)) {
                    String fileName = getFileNameFromUrl(parameter);
                    if (!TextUtils.isEmpty(fileName)) {
                        return fileName;
                    }
                }
            }
        }
        return null;
    }

    private void setState(@State int state) {
        if (state != STATE_FAIL) {
            taskEntity.setErrorMsg(null);
        }
        taskEntity.setState(state);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    private void setStateFail(String errorMsg) {
        taskEntity.setErrorMsg(errorMsg);
        setState(STATE_FAIL);
    }

    public void delete(boolean deleteFile) {
        pause();
        DBManager.getInstance().getTaskDao().delete(taskEntity);
        if (deleteFile) {
            File file = new File(taskEntity.getSaveDir(), taskEntity.getFileName());
            if (!file.delete()) {
                //ignore
            }
        }
        onDelete(deleteFile);
    }

    protected void onDelete(boolean deleteFile) {

    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof DownloadTask) {
            return ((DownloadTask) obj).taskEntity.getUniqueId().equals(taskEntity.getUniqueId());
        }
        return false;
    }

    protected void setETag(String eTag) {
        taskEntity.setETag(eTag);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    protected void setLastModified(String lastModified) {
        taskEntity.setLastModified(lastModified);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    public String getETag() {
        return taskEntity.getETag();
    }

    public String getLastModified() {
        return taskEntity.getLastModified();
    }

    public void setContentType(String contentType) {
        taskEntity.setContentType(contentType);
        DBManager.getInstance().getTaskDao().update(taskEntity);
    }

    public String getContentType() {
        return taskEntity.getContentType();
    }

    public long getCreateTime() {
        return taskEntity.getCreateTime();
    }

    public long getId() {
        return taskEntity.getId();
    }

    public String getErrorMsg() {
        return taskEntity.getErrorMsg();
    }
}

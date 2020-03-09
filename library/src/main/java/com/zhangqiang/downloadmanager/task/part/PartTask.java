package com.zhangqiang.downloadmanager.task.part;

import android.support.annotation.Nullable;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;
import com.zhangqiang.downloadmanager.helper.ProgressUpdateHelper;

public abstract class PartTask {

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_PAUSE = 1;
    public static final int STATUS_DOWNLOADING = 2;
    public static final int STATUS_FAIL = 3;
    public static final int STATUS_COMPLETE = 4;

    private final PartEntity partEntity;
    private Callback callback;
    private ProgressUpdateHelper progressUpdateHelper = new ProgressUpdateHelper() {
        @Override
        protected void doUpdate() {
            DBManager.getInstance().getPartDao().update(partEntity);
        }
    };

    public PartTask(String url, String savePath, long start, long end) {

        partEntity = new PartEntity()
                .setUrl(url)
                .setSavePath(savePath)
                .setStart(start)
                .setEnd(end)
                .setStatus(STATUS_IDLE);
        DBManager.getInstance().getPartDao().resume(partEntity);
    }

    public void setCurrent(long current) {
        partEntity.setCurrent(current);
        DBManager.getInstance().getPartDao().update(partEntity);
    }

    public long getCurrent() {
        return partEntity.getCurrent();
    }

    public void start() {
        int status = getStatus();
        if (status == STATUS_DOWNLOADING || status == STATUS_COMPLETE) {
            return;
        }
        onStart();
        setStatus(STATUS_DOWNLOADING);
        if (callback != null) {
            callback.onStart();
        }
    }

    protected abstract void onStart();

    protected abstract void onPause();

    public void pause() {
        int status = getStatus();
        if (status != STATUS_DOWNLOADING) {
            return;
        }
        onPause();
        setStatus(STATUS_PAUSE);
        if (callback != null) {
            callback.onPause();
        }
    }

    protected void notifyProgress(long current) {
        partEntity.setCurrent(current);
        progressUpdateHelper.update();
//        DBManager.getInstance().getPartDao().update(partEntity);
        if (callback != null) {
            callback.onProgress(current, getStart(), getEnd());
        }
    }

    public long getStart() {
        return partEntity.getStart();
    }

    public long getEnd() {
        return partEntity.getEnd();
    }

    protected void notifyComplete() {
        setStatus(STATUS_COMPLETE);
        if (callback != null) {
            callback.onComplete();
        }
    }

    protected void notifyFail(Throwable e) {
        setStatus(STATUS_FAIL);
        if (callback != null) {
            callback.onFail(e);
        }
    }

    public PartTask setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public void setStatus(int status) {
        partEntity.setStatus(status);
        DBManager.getInstance().getPartDao().update(partEntity);
    }

    public int getStatus() {
        return partEntity.getStatus();
    }

    public String getUrl() {
        return partEntity.getUrl();
    }

    public String getSavePath() {
        return partEntity.getSavePath();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PartTask) {
            return ((PartTask) obj).partEntity.getUniqueId().equals(partEntity.getUniqueId());
        }
        return super.equals(obj);
    }

    public void delete() {
        pause();
        DBManager.getInstance().getPartDao().delete(partEntity);
    }


}

package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.PartEntityDao;
import com.zhangqiang.downloadmanager.db.dao.TaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadTask;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager {

    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_COMPLETE = 4;

    private static volatile DownloadManager instance;
    private Context mContext;
    private int maxRunningTaskCount = 2;
    private String saveDir;
    private int threadSize = 2;
    private DownloadRecord mRecordHead;
    private AtomicInteger recordSize = new AtomicInteger();
    private AtomicInteger activeRecordSize = new AtomicInteger();

    public static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DownloadManager(Context context) {
        mContext = context;
        File saveDir = new File(mContext.getFilesDir(), "download");
        if (!saveDir.exists()) {
            saveDir.mkdir();
        }
        this.saveDir = saveDir.getAbsolutePath();

        TaskEntityDao taskEntityDao = getTaskEntityDao();
        List<TaskEntity> taskEntities = taskEntityDao.queryBuilder().orderAsc(TaskEntityDao.Properties.CreateTime).list();
        if (taskEntities == null || taskEntities.isEmpty()) {
            return;
        }
        DownloadRecord tail = mRecordHead;
        for (int i = 0; i < taskEntities.size(); i++) {
            TaskEntity taskEntity = taskEntities.get(i);
            DownloadTask downloadTask = createDownloadTask(taskEntity);
            DownloadRecord downloadRecord = makeDownloadRecord(taskEntity, downloadTask);
            if (tail == null) {
                tail = mRecordHead = downloadRecord;
            } else {
                tail.next = downloadRecord;
                tail = downloadRecord;
            }
            recordSize.incrementAndGet();
        }
        tryStartIdleTask();
    }

    private DownloadRecord makeDownloadRecord(TaskEntity taskEntity, DownloadTask downloadTask) {
        DownloadRecord downloadRecord = new DownloadRecord(taskEntity, downloadTask);
        configDownloadRecord(downloadRecord);
        return downloadRecord;
    }

    public synchronized void download(String url) {

        if (TextUtils.isEmpty(url)) {
            return;
        }

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            prev = curr;
            curr = curr.next;
        }

        DownloadTask downloadTask = createDownloadTask(url);
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUrl(url);
        taskEntity.setSaveDir(saveDir);
        taskEntity.setCreateTime(System.currentTimeMillis());
        taskEntity.setThreadSize(threadSize);
        taskEntity.setState(STATE_IDLE);
        getTaskEntityDao().insert(taskEntity);
        DownloadRecord recordHead = makeDownloadRecord(taskEntity, downloadTask);
        if (prev == null) {
            this.mRecordHead = recordHead;
        } else {
            prev.next = recordHead;
        }
        recordSize.incrementAndGet();
        tryStartIdleTask();
    }

    public int getTaskCount() {
        return recordSize.get();
    }

    public List<TaskEntity> getTaskList() {
        return getTaskEntityDao().loadAll();
    }

    public synchronized void start(long id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            TaskEntity entity = curr.entity;
            if (entity.getId() == id
                    && entity.getState() != STATE_COMPLETE
                    && entity.getState() != STATE_DOWNLOADING) {
                curr.downloadTask.start();
            }
            curr = curr.next;
        }
    }

    public synchronized void pause(long id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (curr.entity.getId() == id){
                curr.downloadTask.cancel();
            }
            curr = curr.next;
        }
    }

    public synchronized void deleteTask(long id, boolean deleteFile) {

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (id == curr.entity.getId()) {

                TaskEntityDao taskEntityDao = getTaskEntityDao();
                taskEntityDao.deleteByKey(id);
                if (deleteFile) {
                    File file = new File(curr.entity.getSaveDir(), curr.entity.getFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (prev == null) {
                    mRecordHead = curr.next;
                } else {
                    prev.next = curr.next;
                }
                recordSize.decrementAndGet();
                if (curr.entity.getState() == STATE_DOWNLOADING) {
                    activeRecordSize.decrementAndGet();
                }
                break;
            }
            prev = curr;
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    private DownloadTask createDownloadTask(String url) {
        return new OKHttpDownloadTask(mContext, url, saveDir, threadSize);
    }

    private DownloadTask createDownloadTask(final TaskEntity taskEntity) {
        return new OKHttpDownloadTask(mContext, taskEntity.getUrl(), taskEntity.getSaveDir(), taskEntity.getThreadSize());
    }

    private void configDownloadRecord(DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        final TaskEntity entity = record.entity;
        downloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                entity.setState(STATE_DOWNLOADING);
                getTaskEntityDao().update(entity);
            }

            @Override
            public void onComplete() {
                entity.setState(STATE_COMPLETE);
                getTaskEntityDao().update(entity);
                activeRecordSize.decrementAndGet();
                tryStartIdleTask();
            }

            @Override
            public void onFail(DownloadException e) {
                entity.setState(STATE_FAIL);
                entity.setErrorMsg("errorCode=" + e.getCode() + ",msg =" + e.getMessage());
                getTaskEntityDao().update(entity);
                activeRecordSize.decrementAndGet();
                tryStartIdleTask();
            }

            @Override
            public void onCancel() {
                entity.setState(STATE_PAUSE);
                getTaskEntityDao().update(entity);
                activeRecordSize.decrementAndGet();
                tryStartIdleTask();
            }
        });
        if (downloadTask instanceof OKHttpDownloadTask) {
            final OKHttpDownloadTask okHttpDownloadTask = (OKHttpDownloadTask) downloadTask;
            okHttpDownloadTask.addOnProgressChangeListener(new OKHttpDownloadTask.OnProgressChangeListener() {
                @Override
                public void onProgressChange(int threadIndex, int threadSize, long current, long start, long end, long total) {

                }
            });
            okHttpDownloadTask.addOnResponseReadyListener(new OKHttpDownloadTask.OnResponseReadyListener() {
                @Override
                public void onResponseReady(HttpResponse response) {
                    entity.setFileName(okHttpDownloadTask.getFileName());
                    entity.setContentType(response.getContentType());
                    entity.setETag(HttpUtils.parseETag(response));
                    entity.setLastModified(HttpUtils.parseLastModified(response));
                    getTaskEntityDao().update(entity);
                }
            });
        }
    }

    private void tryStartIdleTask() {
        if (activeRecordSize.get() >= maxRunningTaskCount) {
            return;
        }
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            TaskEntity entity = curr.entity;
            DownloadTask downloadTask = curr.downloadTask;
            if (entity.getState() == STATE_IDLE) {
                downloadTask.start();
                if (activeRecordSize.incrementAndGet() >= maxRunningTaskCount) {
                    break;
                }
            }
            curr = curr.next;
        }
    }

    private static class DownloadRecord {

        private DownloadRecord next;
        private final TaskEntity entity;
        private final DownloadTask downloadTask;

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask) {
            this.entity = entity;
            this.downloadTask = downloadTask;
        }
    }


    private TaskEntityDao getTaskEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getTaskEntityDao();
    }

    private PartEntityDao getPartEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getPartEntityDao();
    }

    private PartEntity getPartEntity(TaskEntity entity, int threadIndex, int threadSize) {
        return getPartEntityDao().queryBuilder().where(PartEntityDao.Properties.TaskId.eq(entity.getId()),
                PartEntityDao.Properties.ThreadIndex.eq(threadIndex),
                PartEntityDao.Properties.ThreadSize.eq(threadSize))
                .uniqueOrThrow();
    }
}

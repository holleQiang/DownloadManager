package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.PartEntityDao;
import com.zhangqiang.downloadmanager.db.dao.TaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.ResourceInfo;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager {

    public static final String TAG = DownloadManager.class.getSimpleName();

    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_COMPLETE = 4;

    private static final int MSG_ACTIVE_POLL = 1000;
    private static final int ACTIVE_POLL_INTERVAL = 200;

    private static final int MSG_TASK_FINISH = 1001;

    private static volatile DownloadManager instance;
    private Context mContext;
    private int maxRunningTaskCount = 2;
    private String saveDir;
    private DownloadRecord mRecordHead;
    private final AtomicInteger mTaskSize = new AtomicInteger();
    private final AtomicInteger mActiveTaskSize = new AtomicInteger();
    private final Handler mMonitorHandler;
    private List<DownloadTaskListener> downloadTaskListeners;

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

        HandlerThread mMonitorThread = new HandlerThread("download_monitor_thread");
        mMonitorThread.start();
        mMonitorHandler = new Handler(mMonitorThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_ACTIVE_POLL) {
                    syncProgress();
                    trySendActiveMsg();
                } else if (msg.what == MSG_TASK_FINISH) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    syncRecordProgress(record);
                }
                return false;
            }
        });

        TaskEntityDao taskEntityDao = getTaskEntityDao();
        List<TaskEntity> taskEntities = taskEntityDao.queryBuilder().orderAsc(TaskEntityDao.Properties.CreateTime).list();
        if (taskEntities == null || taskEntities.isEmpty()) {
            return;
        }
        DownloadRecord tail = mRecordHead;
        for (int i = 0; i < taskEntities.size(); i++) {
            TaskEntity taskEntity = taskEntities.get(i);
            DownloadRecord downloadRecord = makeDownloadRecord(taskEntity);
            if (tail == null) {
                tail = mRecordHead = downloadRecord;
            } else {
                tail.next = downloadRecord;
                tail = downloadRecord;
            }
            incrementTaskSize();
            if (taskEntity.getState() == STATE_DOWNLOADING) {
                downloadRecord.downloadTask.start();
            }
        }
        tryStartIdleTask();
    }

    private DownloadRecord makeDownloadRecord(String url,int threadSize) {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUrl(url);
        taskEntity.setSaveDir(saveDir);
        taskEntity.setCreateTime(System.currentTimeMillis());
        taskEntity.setThreadSize(threadSize);
        taskEntity.setState(STATE_IDLE);
        getTaskEntityDao().insert(taskEntity);

        DownloadTask task = new OKHttpDownloadTask(mContext, url, saveDir, threadSize);
        DownloadRecord downloadRecord = new DownloadRecord(taskEntity, task);
        configDownloadRecord(downloadRecord);
        return downloadRecord;
    }

    private DownloadRecord makeDownloadRecord(TaskEntity taskEntity) {
        List<PartEntity> partList = taskEntity.getPartList();
        List<PartRecord> partRecords = null;
        List<OKHttpDownloadPartTask> partTasks = null;
        if (partList != null) {
            partTasks = new ArrayList<>();
            partRecords = new ArrayList<>();
            for (int i = 0; i < partList.size(); i++) {
                PartEntity partEntity = partList.get(i);
                OKHttpDownloadPartTask partTask = new OKHttpDownloadPartTask(mContext, taskEntity.getUrl(), partEntity.getStart(), partEntity.getCurrent(), partEntity.getEnd(), partEntity.getSavePath());
                partTasks.add(partTask);
                partRecords.add(new PartRecord(partEntity, partTask));
            }
        }
        DownloadTask task = new OKHttpDownloadTask(mContext,
                taskEntity.getUrl(),
                taskEntity.getSaveDir(),
                taskEntity.getThreadSize(),
                taskEntity.getFileName(),
                taskEntity.getContentLength(),
                partTasks);
        DownloadRecord downloadRecord = new DownloadRecord(taskEntity, task, partRecords);
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

        DownloadRecord recordHead = makeDownloadRecord(url,2);
        if (prev == null) {
            this.mRecordHead = recordHead;
        } else {
            prev.next = recordHead;
        }
        incrementTaskSize();
        tryStartIdleTask();
    }

    public int getTaskSize() {
        return mTaskSize.get();
    }

    public List<TaskEntity> getTaskList() {
        List<TaskEntity> list = new ArrayList<>();
        DownloadRecord curr = mRecordHead;
        while (curr != null){
            list.add(curr.entity);
            curr = curr.next;
        }
        return list;
    }

    public synchronized void start(long id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            TaskEntity entity = curr.entity;
            if (entity.getId() == id
                    && entity.getState() != STATE_COMPLETE
                    && entity.getState() != STATE_DOWNLOADING
                    && entity.getState() != STATE_IDLE) {
                entity.setState(STATE_IDLE);
                getTaskEntityDao().update(entity);
                if (downloadTaskListeners != null) {
                    for (int i = 0; i < downloadTaskListeners.size(); i++) {
                        downloadTaskListeners.get(i).onTaskStateChanged(entity.getId());
                    }
                }
                break;
            }
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    public synchronized void pause(long id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (curr.entity.getId() == id) {
                TaskEntity entity = curr.entity;
                DownloadTask task = curr.downloadTask;
                if (entity.getState() == STATE_DOWNLOADING) {
                    task.cancel();
                }
            }
            curr = curr.next;
        }
    }

    public synchronized void deleteTask(long id, boolean deleteFile) {

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            TaskEntity entity = curr.entity;
            if (id == entity.getId()) {

                curr.downloadTask.cancel();

                TaskEntityDao taskEntityDao = getTaskEntityDao();
                taskEntityDao.deleteByKey(id);
                if (deleteFile) {
                    File file = new File(entity.getSaveDir(), entity.getFileName());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                if (prev == null) {
                    mRecordHead = curr.next;
                } else {
                    prev.next = curr.next;
                }
                decrementTaskSize();
                break;
            }
            prev = curr;
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    private void configDownloadRecord(final DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        final TaskEntity entity = record.entity;
        downloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                LogUtils.i(TAG, "=======onStart=======");
                entity.setState(STATE_DOWNLOADING);
                getTaskEntityDao().update(entity);
                incrementActiveTaskSize();
                if (downloadTaskListeners != null) {
                    for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                        downloadTaskListeners.get(i).onTaskStateChanged(entity.getId());
                    }
                }
            }

            @Override
            public void onComplete() {
                LogUtils.i(TAG, "=======onComplete=======");
                entity.setState(STATE_COMPLETE);
                getTaskEntityDao().update(entity);
                decrementActiveTaskSize();
                Message message = Message.obtain(mMonitorHandler, MSG_TASK_FINISH);
                message.obj = record;
                mMonitorHandler.sendMessage(message);

                if (downloadTaskListeners != null) {
                    for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                        downloadTaskListeners.get(i).onTaskStateChanged(entity.getId());
                    }
                }
            }

            @Override
            public void onFail(DownloadException e) {
                LogUtils.i(TAG, "=======onFail=======");
                entity.setState(STATE_FAIL);
                entity.setErrorMsg("errorCode=" + e.getCode() + ",msg =" + e.getMessage());
                getTaskEntityDao().update(entity);
                decrementActiveTaskSize();

                if (downloadTaskListeners != null) {
                    for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                        downloadTaskListeners.get(i).onTaskStateChanged(entity.getId());
                    }
                }
            }

            @Override
            public void onCancel() {
                LogUtils.i(TAG, "=======onCancel=======");
                entity.setState(STATE_PAUSE);
                getTaskEntityDao().update(entity);
                decrementActiveTaskSize();

                if (downloadTaskListeners != null) {
                    for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                        downloadTaskListeners.get(i).onTaskStateChanged(entity.getId());
                    }
                }
            }
        });
        if (downloadTask instanceof OKHttpDownloadTask) {
            final OKHttpDownloadTask okHttpDownloadTask = (OKHttpDownloadTask) downloadTask;
            okHttpDownloadTask.setOnResourceInfoReadyListener(new OKHttpDownloadTask.OnResourceInfoReadyListener() {
                @Override
                public void onResourceInfoReady(ResourceInfo info) {
                    entity.setFileName(info.getFileName());
                    entity.setContentType(info.getContentType());
                    entity.setContentLength(info.getContentLength());
                    entity.setETag(info.getETag());
                    entity.setLastModified(info.getLastModified());
                    getTaskEntityDao().update(entity);

                    if (downloadTaskListeners != null) {
                        for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                            downloadTaskListeners.get(i).onTaskInfoChanged(entity.getId());
                        }
                    }
                }
            });
            okHttpDownloadTask.setOnPartTaskCreateListener(new OKHttpDownloadTask.OnPartTaskCreateListener() {
                @Override
                public void onPartTaskCreate(int threadIndex, int threadSize, OKHttpDownloadPartTask task) {
                    PartEntity partEntity = new PartEntity();
                    partEntity.setTaskId(entity.getId());
                    partEntity.setSavePath(task.getSavePath());
                    partEntity.setStart(task.getFromPosition());
                    partEntity.setCurrent(task.getCurrentLength());
                    partEntity.setThreadSize(threadSize);
                    partEntity.setThreadIndex(threadIndex);
                    partEntity.setEnd(task.getToPosition());
                    getPartEntityDao().insert(partEntity);

                    addPartRecord(record, new PartRecord(partEntity, task));
                }
            });
        }
    }

    private synchronized void addPartRecord(DownloadRecord record, PartRecord partRecord) {
        if (record.partRecords == null) {
            record.partRecords = new ArrayList<>();
        }
        record.partRecords.add(partRecord);
    }

    private void trySendActiveMsg() {
        if (getActiveTaskSize() > 0 && !mMonitorHandler.hasMessages(MSG_ACTIVE_POLL)) {
            mMonitorHandler.sendEmptyMessageDelayed(MSG_ACTIVE_POLL, ACTIVE_POLL_INTERVAL);
        }
    }

    private void tryStartIdleTask() {

        DownloadRecord curr = mRecordHead;
        while (curr != null && getActiveTaskSize() < maxRunningTaskCount) {
            TaskEntity entity = curr.entity;
            DownloadTask downloadTask = curr.downloadTask;
            if (entity.getState() == STATE_IDLE) {
                downloadTask.start();
            }
            curr = curr.next;
        }
    }

    public int getActiveTaskSize(){
        return mActiveTaskSize.get();
    }

    private static class DownloadRecord {

        private DownloadRecord next;
        private final TaskEntity entity;
        private final DownloadTask downloadTask;
        private List<PartRecord> partRecords;

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask) {
            this.entity = entity;
            this.downloadTask = downloadTask;
        }

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask, List<PartRecord> partRecords) {
            this.entity = entity;
            this.downloadTask = downloadTask;
            this.partRecords = partRecords;
        }
    }

    private static class PartRecord {
        private final PartEntity partEntity;
        private final DownloadTask partTask;

        public PartRecord(PartEntity partEntity, DownloadTask partTask) {
            this.partEntity = partEntity;
            this.partTask = partTask;
        }
    }

    private TaskEntityDao getTaskEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getTaskEntityDao();
    }

    private PartEntityDao getPartEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getPartEntityDao();
    }

    private void syncProgress() {

//        LogUtils.i(TAG, "=======syncProgress=======");
        synchronized (DownloadManager.this) {
            DownloadRecord curr = mRecordHead;
            while (curr != null) {
                syncRecordProgress(curr);
                curr = curr.next;
            }
        }
    }

    private void syncRecordProgress(DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        TaskEntity entity = record.entity;
        if (downloadTask.getCurrentLength() != entity.getCurrentLength()) {
            entity.setCurrentLength(downloadTask.getCurrentLength());
            getTaskEntityDao().update(entity);

            List<PartRecord> partRecords = record.partRecords;
            if (partRecords != null) {
                for (int i = 0; i < partRecords.size(); i++) {
                    PartRecord partRecord = partRecords.get(i);
                    long currentLength = partRecord.partTask.getCurrentLength();
                    PartEntity partEntity = partRecord.partEntity;
                    if (partEntity.getCurrent() != currentLength) {
                        partEntity.setCurrent(currentLength);
                        getPartEntityDao().update(partEntity);
                    }
                }
            }

            if (downloadTaskListeners != null) {
                for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                    downloadTaskListeners.get(i).onTaskProgressChanged(entity.getId());
                }
            }
            LogUtils.i(TAG, "=======syncRecordProgress========" + entity.getCurrentLength() + "---" + entity.getContentLength());
        }
    }

    public synchronized void addDownloadTaskListener(DownloadTaskListener listener) {
        if (downloadTaskListeners == null) {
            downloadTaskListeners = new ArrayList<>();
        }
        if (downloadTaskListeners.contains(listener)) {
            return;
        }
        downloadTaskListeners.add(listener);
    }

    public synchronized void removeDownloadTaskListener(DownloadTaskListener listener) {
        if (downloadTaskListeners == null) {
            return;
        }
        downloadTaskListeners.remove(listener);
    }

    private void decrementActiveTaskSize() {
        if (mActiveTaskSize.decrementAndGet() < 0) {
            throw new IllegalStateException("active task size is smaller than 0");
        }
        tryStartIdleTask();
    }

    private void incrementActiveTaskSize() {
        if (mActiveTaskSize.incrementAndGet() > maxRunningTaskCount) {
            throw new IllegalStateException("active task size is bigger than max running task");
        }
        trySendActiveMsg();
    }

    private void incrementTaskSize(){
        mTaskSize.incrementAndGet();
    }

    private void decrementTaskSize(){
        if (mTaskSize.decrementAndGet() < 0) {
            throw new IllegalStateException("task size is smaller than 0");
        }
    }
}

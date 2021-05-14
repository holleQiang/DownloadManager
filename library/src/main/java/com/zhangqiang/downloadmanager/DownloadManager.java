package com.zhangqiang.downloadmanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.PartEntityDao;
import com.zhangqiang.downloadmanager.db.dao.TaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.listener.DownloadTaskListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.ResourceInfo;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final Context mContext;
    private final int maxRunningTaskCount = 2;
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
        mContext = context.getApplicationContext();

        HandlerThread mMonitorThread = new HandlerThread("download_monitor_thread");
        mMonitorThread.start();
        mMonitorHandler = new Handler(mMonitorThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_ACTIVE_POLL) {
                    syncTask();
                    trySendActiveMsg();
                } else if (msg.what == MSG_TASK_FINISH) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    syncTaskProgress(record);
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

    private DownloadRecord makeDownloadRecord(String url, int threadSize, String saveDir) {

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
        HashMap<Integer, PartRecord> partRecords = null;
        List<OKHttpDownloadPartTask> partTasks = null;
        if (partList != null) {
            partTasks = new ArrayList<>();
            partRecords = new HashMap<>();
            for (int i = 0; i < partList.size(); i++) {
                PartEntity partEntity = partList.get(i);
                OKHttpDownloadPartTask partTask = new OKHttpDownloadPartTask(mContext, taskEntity.getUrl(), partEntity.getStart(), partEntity.getCurrent(), partEntity.getEnd(), partEntity.getSavePath());
                partTasks.add(partTask);
                partRecords.put(partEntity.getThreadIndex(), new PartRecord(partEntity, partTask));
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

    public synchronized long download(String url, int threadSize, String saveDir) {

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(saveDir) || threadSize < 1) {
            return -1;
        }

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            prev = curr;
            curr = curr.next;
        }

        DownloadRecord record = makeDownloadRecord(url, threadSize, saveDir);
        if (prev == null) {
            this.mRecordHead = record;
        } else {
            prev.next = record;
        }
        incrementTaskSize();
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskAdded(record.entity.getId());
            }
        }
        tryStartIdleTask();
        return record.entity.getId();
    }

    public int getTaskSize() {
        return mTaskSize.get();
    }

    public synchronized List<TaskInfo> getTaskList() {
        List<TaskInfo> list = new ArrayList<>();
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            list.add(new TaskInfoImpl(curr));
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
                List<PartEntity> partList = entity.getPartList();
                if (partList != null) {
                    for (PartEntity part : partList) {
                        getPartEntityDao().delete(part);
                    }
                }
                if (deleteFile) {
                    File file = new File(entity.getSaveDir(), entity.getFileName());
                    if (file.exists()) {
                        if (!file.delete()) {
                            Log.e(TAG, "delete file fail:" + file.getAbsolutePath());
                        }
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
                LogUtils.i(TAG, "=======onFail=======" + e.getMessage());
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

                    addPartRecord(record, new PartRecord(partEntity, task), threadIndex);
                }
            });
        }
    }

    private synchronized void addPartRecord(DownloadRecord record, PartRecord partRecord, int index) {
        if (record.partRecords == null) {
            record.partRecords = new HashMap<>();
        }
        record.partRecords.put(index, partRecord);
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

    public int getActiveTaskSize() {
        return mActiveTaskSize.get();
    }

    public synchronized TaskInfo getTaskInfo(long id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (curr.entity.getId() == id) {
                return new TaskInfoImpl(curr);
            }
            curr = curr.next;
        }
        return null;
    }

    private static class DownloadRecord {

        private DownloadRecord next;
        private final TaskEntity entity;
        private final DownloadTask downloadTask;
        private HashMap<Integer, PartRecord> partRecords;
        private long speed;
        private long lastComputeTime;
        private long lastLength;

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask) {
            this(entity, downloadTask, null);
        }

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask, HashMap<Integer, PartRecord> partRecords) {
            this.entity = entity;
            this.downloadTask = downloadTask;
            this.partRecords = partRecords;
        }
    }

    private static class PartRecord {
        private final PartEntity partEntity;
        private final DownloadTask partTask;
        private long speed;
        private long lastComputeTime;
        private long lastLength;

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

    private void syncTask() {

        synchronized (DownloadManager.this) {
            DownloadRecord curr = mRecordHead;
            while (curr != null) {
                //计算progress
                syncTaskProgress(curr);
                //计算主任务下载速度
                computeTaskSpeed(curr);
                //计算子任务下载速度
                HashMap<Integer, PartRecord> partRecords = curr.partRecords;
                if (partRecords != null && !partRecords.isEmpty()) {
                    for (Map.Entry<Integer, PartRecord> entry : partRecords.entrySet()) {
                        computePartTaskSpeed(entry.getValue());
                    }
                }
                curr = curr.next;
            }
        }
    }

    private void syncTaskProgress(DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        TaskEntity entity = record.entity;
        long oldLength = entity.getCurrentLength();
        long newLength = downloadTask.getCurrentLength();
        if (newLength != oldLength) {
            entity.setCurrentLength(newLength);
            getTaskEntityDao().update(entity);

            HashMap<Integer, PartRecord> partRecords = record.partRecords;
            if (partRecords != null && !partRecords.isEmpty()) {
                for (Map.Entry<Integer, PartRecord> entry : partRecords.entrySet()) {
                    PartRecord partRecord = entry.getValue();
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

    private void computeTaskSpeed(DownloadRecord record) {
        TaskEntity entity = record.entity;
        if (record.lastComputeTime == 0) {
            record.lastComputeTime = SystemClock.elapsedRealtime();
            record.lastLength = entity.getCurrentLength();
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();
        long deltaTime = currentTime - record.lastComputeTime;
        if (deltaTime > 1000) {
            long currentLength = entity.getCurrentLength();
            long deltaLength = currentLength - record.lastLength;
            long newSpeed = deltaLength / deltaTime * 1000;
            if (record.speed != newSpeed) {
                record.speed = newSpeed;
                if (downloadTaskListeners != null) {
                    for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                        downloadTaskListeners.get(i).onTaskSpeedChanged(entity.getId());
                    }
                }
            }
            record.lastComputeTime = currentTime;
            record.lastLength = currentLength;
        }
    }

    private void computePartTaskSpeed(PartRecord record) {
        PartEntity entity = record.partEntity;
        if (record.lastComputeTime == 0) {
            record.lastComputeTime = SystemClock.elapsedRealtime();
            record.lastLength = entity.getCurrent();
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();
        long deltaTime = currentTime - record.lastComputeTime;
        if (deltaTime > 1000) {
            long currentLength = entity.getCurrent();
            long deltaLength = currentLength - record.lastLength;
            long newSpeed = deltaLength / deltaTime * 1000;
            if (record.speed != newSpeed) {
                record.speed = newSpeed;
            }
            record.lastComputeTime = currentTime;
            record.lastLength = currentLength;
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

    private void incrementTaskSize() {
        mTaskSize.incrementAndGet();
    }

    private void decrementTaskSize() {
        if (mTaskSize.decrementAndGet() < 0) {
            throw new IllegalStateException("task size is smaller than 0");
        }
    }

    private static class TaskInfoImpl implements TaskInfo {

        private final DownloadRecord record;

        public TaskInfoImpl(DownloadRecord record) {
            this.record = record;
        }

        @Override
        public Long getId() {
            return record.entity.getId();
        }

        @Override
        public String getUrl() {
            return record.entity.getUrl();
        }

        @Override
        public String getSaveDir() {
            return record.entity.getSaveDir();
        }

        @Override
        public String getFileName() {
            return record.entity.getFileName();
        }

        @Override
        public long getCurrentLength() {
            return record.entity.getCurrentLength();
        }

        @Override
        public long getContentLength() {
            return record.entity.getContentLength();
        }

        @Override
        public int getState() {
            return record.entity.getState();
        }

        @Override
        public String getETag() {
            return record.entity.getETag();
        }

        @Override
        public String getLastModified() {
            return record.entity.getLastModified();
        }

        @Override
        public String getContentType() {
            return record.entity.getContentType();
        }

        @Override
        public long getCreateTime() {
            return record.entity.getCreateTime();
        }

        @Override
        public String getErrorMsg() {
            return record.entity.getErrorMsg();
        }

        @Override
        public int getThreadSize() {
            return record.entity.getThreadSize();
        }

        @Override
        public long getSpeed() {
            return record.speed;
        }

        @Override
        public long getThreadSpeed(int threadIndex) {
            HashMap<Integer, PartRecord> partRecords = record.partRecords;
            if (partRecords != null) {
                PartRecord partRecord = partRecords.get(threadIndex);
                if (partRecord != null) {
                    return partRecord.speed;
                }
            }
            return 0;
        }

        @Override
        public long getThreadCurrentLength(int threadIndex) {
            HashMap<Integer, PartRecord> partRecords = record.partRecords;
            if (partRecords != null) {
                PartRecord partRecord = partRecords.get(threadIndex);
                if (partRecord != null) {
                    return partRecord.partEntity.getCurrent();
                }
            }
            return 0;
        }
    }
}

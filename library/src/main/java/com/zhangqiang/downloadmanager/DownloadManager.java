package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.PartEntityDao;
import com.zhangqiang.downloadmanager.db.dao.TaskEntityDao;
import com.zhangqiang.downloadmanager.db.entity.PartEntity;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.listeners.DownloadTaskListener;
import com.zhangqiang.downloadmanager.listeners.OnActiveTaskSizeChangedListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.OKHttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.ResourceInfo;
import com.zhangqiang.downloadmanager.task.speed.SpeedRecord;
import com.zhangqiang.downloadmanager.task.speed.SpeedSupport;
import com.zhangqiang.downloadmanager.task.speed.SpeedUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadManager {

    public static final String TAG = DownloadManager.class.getSimpleName();

    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_COMPLETE = 4;

    private static final int INTERVAL_PROGRESS_SYNC = 200;
    private static final int INTERVAL_SPEED_COMPUTE = 1000;

    private static final int MSG_PROGRESS_SYNC = 1000;
    private static final int MSG_SPEED_COMPUTE = 1002;
    private static final int MSG_TASK_START = 1010;
    private static final int MSG_TASK_CANCEL = 1011;
    private static final int MSG_TASK_ERROR = 1012;
    private static final int MSG_TASK_FINISH = 1013;
    private static final int MSG_TASK_ADDED = 1014;
    private static final int MSG_TASK_REMOVED = 1015;
    private static final int MSG_ACTIVE_TASK_SIZE_CHANGED = 1016;

    private static volatile DownloadManager instance;
    private final Context mContext;
    private final int maxRunningTaskCount = 2;
    private DownloadRecord mRecordHead;
    private final AtomicInteger mTaskSize = new AtomicInteger();
    private final AtomicInteger mActiveTaskSize = new AtomicInteger();
    private final Handler mMonitorHandler;
    private List<DownloadTaskListener> downloadTaskListeners;
    private List<OnActiveTaskSizeChangedListener> onActiveTaskSizeChangedListeners;

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
                int what = msg.what;
                if (what == MSG_PROGRESS_SYNC) {
                    syncTasksProgress();
                    trySendProgressSyncMsg();
                } else if (what == MSG_SPEED_COMPUTE) {
                    computeTasksSpeed();
                    trySendSpeedComputeMsg();
                } else if (what == MSG_TASK_START) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    SpeedUtils.resetStatus(record);
                    notifyTaskStatusChanged(record.entity.getId());
                    LogUtils.i(TAG, "=======onStart=======");
                } else if (what == MSG_TASK_CANCEL) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    syncTaskProgress(record);
                    computeTaskSpeed(record);
                    notifyTaskStatusChanged(record.entity.getId());
                    LogUtils.i(TAG, "=======onCancel=======");
                } else if (what == MSG_TASK_ERROR) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    syncTaskProgress(record);
                    computeTaskSpeed(record);
                    notifyTaskStatusChanged(record.entity.getId());
                    LogUtils.i(TAG, "=======onFail=======" + record.entity.getErrorMsg());
                } else if (what == MSG_TASK_FINISH) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    syncTaskProgress(record);
                    computeTaskSpeed(record);
                    notifyTaskStatusChanged(record.entity.getId());
                    LogUtils.i(TAG, "=======onComplete=======");
                } else if (what == MSG_TASK_ADDED) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    notifyTaskAdded(record);
                } else if (what == MSG_TASK_REMOVED) {
                    DownloadRecord record = (DownloadRecord) msg.obj;
                    notifyTaskRemoved(record);
                } else if (what == MSG_ACTIVE_TASK_SIZE_CHANGED) {
                    notifyActiveTaskSizeChanged();
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


    private DownloadRecord makeDownloadRecord(Request request) {

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setUrl(request.getUrl());
        taskEntity.setSaveDir(request.getSaveDir());
        taskEntity.setCreateTime(System.currentTimeMillis());
        taskEntity.setThreadSize(request.getThreadCount());
        taskEntity.setFileName(request.getFileName());
        taskEntity.setState(STATE_IDLE);
        getTaskEntityDao().insert(taskEntity);

        DownloadTask task = new OKHttpDownloadTask(mContext,
                request.getUrl(),
                request.getSaveDir(),
                request.getThreadCount(),
                request.getFileName(),
                new HttpDownloadTask.PartTaskFactory() {
                    @Override
                    public HttpDownloadPartTask createPartTask(String url, long start, long end, String savePath) {
                        return new OKHttpDownloadPartTask(mContext,url,start,0,end,savePath);
                    }
                }
        );
        DownloadRecord downloadRecord = new DownloadRecord(taskEntity, task);
        configDownloadRecord(downloadRecord);
        return downloadRecord;
    }

    private DownloadRecord makeDownloadRecord(TaskEntity taskEntity) {
        List<PartEntity> partList = taskEntity.getPartList();
        HashMap<Integer, PartRecord> partRecords = null;
        List<HttpDownloadPartTask> partTasks = null;
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

    public synchronized long enqueue(Request request) {

        if (TextUtils.isEmpty(request.getUrl())
                || TextUtils.isEmpty(request.getSaveDir())
                || request.getThreadCount() < 1) {
            return -1;
        }

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            prev = curr;
            curr = curr.next;
        }

        DownloadRecord record = makeDownloadRecord(request);
        if (prev == null) {
            this.mRecordHead = record;
        } else {
            prev.next = record;
        }
        incrementTaskSize();
        sendTaskMsg(MSG_TASK_ADDED, record);
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
                    String saveDir = entity.getSaveDir();
                    String fileName = entity.getFileName();
                    if(!TextUtils.isEmpty(saveDir) || !TextUtils.isEmpty(fileName)){
                        File file = new File(saveDir, fileName);
                        if (file.exists()) {
                            if (!file.delete()) {
                                Log.e(TAG, "delete file fail:" + file.getAbsolutePath());
                            }
                        }
                    }
                }
                if (prev == null) {
                    mRecordHead = curr.next;
                } else {
                    prev.next = curr.next;
                }
                curr.next = null;
                decrementTaskSize();
                sendTaskMsg(MSG_TASK_REMOVED, curr);
                break;
            }
            prev = curr;
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    private void configDownloadRecord(final DownloadRecord record) {
        final DownloadTask downloadTask = record.downloadTask;
        final TaskEntity entity = record.entity;
        downloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                entity.setState(STATE_DOWNLOADING);
                getTaskEntityDao().update(entity);
                sendTaskMsg(MSG_TASK_START, record);
                incrementActiveTaskSize();
            }

            @Override
            public void onComplete() {
                entity.setState(STATE_COMPLETE);
                getTaskEntityDao().update(entity);
                sendTaskMsg(MSG_TASK_FINISH, record);
                decrementActiveTaskSize();
            }

            @Override
            public void onFail(DownloadException e) {
                entity.setState(STATE_FAIL);
                entity.setErrorMsg("errorCode=" + e.getCode() + ",msg =" + e.getMessage());
                getTaskEntityDao().update(entity);
                sendTaskMsg(MSG_TASK_ERROR, record);
                decrementActiveTaskSize();
            }

            @Override
            public void onCancel() {
                entity.setState(STATE_PAUSE);
                getTaskEntityDao().update(entity);
                sendTaskMsg(MSG_TASK_CANCEL, record);
                decrementActiveTaskSize();
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
                public void onPartTaskCreate(int threadIndex, int threadSize, HttpDownloadPartTask task) {
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


    private  void addPartRecord(DownloadRecord record, PartRecord partRecord, int index) {
        record.putPartRecord(index, partRecord);
    }

    private void trySendProgressSyncMsg() {
        if (getActiveTaskSize() > 0 && !mMonitorHandler.hasMessages(MSG_PROGRESS_SYNC)) {
            mMonitorHandler.sendEmptyMessageDelayed(MSG_PROGRESS_SYNC, INTERVAL_PROGRESS_SYNC);
        }
    }

    private void trySendSpeedComputeMsg() {
        if (getActiveTaskSize() > 0 && !mMonitorHandler.hasMessages(MSG_SPEED_COMPUTE)) {
            mMonitorHandler.sendEmptyMessageDelayed(MSG_SPEED_COMPUTE, INTERVAL_SPEED_COMPUTE);
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

    private static class DownloadRecord implements SpeedSupport {

        private DownloadRecord next;
        private final TaskEntity entity;
        private final DownloadTask downloadTask;
        private final Map<Integer, PartRecord> partRecords = new ConcurrentHashMap<>();
        private final SpeedRecord speedRecord = new SpeedRecord();

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask) {
            this(entity, downloadTask, null);
        }

        public DownloadRecord(TaskEntity entity, DownloadTask downloadTask, HashMap<Integer, PartRecord> partRecords) {
            this.entity = entity;
            this.downloadTask = downloadTask;
            if (partRecords != null) {
                this.partRecords.putAll(partRecords);
            }
        }

        public void putPartRecord(int threadIndex,PartRecord record){
            partRecords.put(threadIndex,record);
        }

        @Override
        public long getCurrentLength() {
            return entity.getCurrentLength();
        }

        @Override
        public SpeedRecord getSpeedRecord() {
            return speedRecord;
        }
    }

    private static class PartRecord implements SpeedSupport {
        private final PartEntity partEntity;
        private final DownloadTask partTask;
        private final SpeedRecord speedRecord = new SpeedRecord();

        public PartRecord(PartEntity partEntity, DownloadTask partTask) {
            this.partEntity = partEntity;
            this.partTask = partTask;
        }

        @Override
        public long getCurrentLength() {
            return partEntity.getCurrent();
        }

        @Override
        public SpeedRecord getSpeedRecord() {
            return speedRecord;
        }
    }

    private TaskEntityDao getTaskEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getTaskEntityDao();
    }

    private PartEntityDao getPartEntityDao() {
        return DBManager.getInstance(mContext).getDaoSession().getPartEntityDao();
    }

    private synchronized void syncTasksProgress() {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            syncTaskProgress(curr);
            curr = curr.next;
        }
    }

    private synchronized void syncTaskProgress(DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        TaskEntity entity = record.entity;
        long oldLength = entity.getCurrentLength();
        long newLength = downloadTask.getCurrentLength();
        if (newLength != oldLength) {
            entity.setCurrentLength(newLength);
            getTaskEntityDao().update(entity);

            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
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

    private synchronized void computeTasksSpeed() {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            DownloadRecord record = curr;
            if (record.downloadTask.isStarted()) {
                computeTaskSpeed(record);
            }
            curr = curr.next;
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
        int activeTaskSize = mActiveTaskSize.decrementAndGet();
        if (activeTaskSize < 0) {
            throw new IllegalStateException("active task size is smaller than 0");
        }
        dispatchActiveTaskSizeChanged();
        if (activeTaskSize == 0) {
            mMonitorHandler.removeMessages(MSG_SPEED_COMPUTE);
            mMonitorHandler.removeMessages(MSG_PROGRESS_SYNC);
        }
        tryStartIdleTask();
    }

    private void incrementActiveTaskSize() {
        if (mActiveTaskSize.incrementAndGet() > maxRunningTaskCount) {
            throw new IllegalStateException("active task size is bigger than max running task");
        }
        dispatchActiveTaskSizeChanged();
        trySendProgressSyncMsg();
        trySendSpeedComputeMsg();
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
            return SpeedUtils.getSpeed(record);
        }

        @Override
        public int getPartSize() {
            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
                return partRecords.size();
            }else {
                return 1;
            }
        }

        @Override
        public long getPartSpeed(int partIndex) {
            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
                PartRecord partRecord = partRecords.get(partIndex);
                if (partRecord != null) {
                    return SpeedUtils.getSpeed(partRecord);
                }
            }else if(partIndex == 0){
                return getSpeed();
            }
            return 0;
        }

        @Override
        public long getPartLength(int partIndex) {
            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
                PartRecord partRecord = partRecords.get(partIndex);
                if (partRecord != null) {
                    return partRecord.partEntity.getCurrent();
                }
            }else if(partIndex == 0){
                return getCurrentLength();
            }
            return 0;
        }

        @Override
        public long getTotalPartLength(int partIndex) {
            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
                PartRecord partRecord = partRecords.get(partIndex);
                if (partRecord != null) {
                    PartEntity partEntity = partRecord.partEntity;
                    return partEntity.getEnd() - partEntity.getStart() + 1;
                }
            }else if(partIndex == 0){
                return getContentLength();
            }
            return 0;
        }
    }

    public synchronized void addOnActiveTaskSizeChangedListener(OnActiveTaskSizeChangedListener listener) {
        if (onActiveTaskSizeChangedListeners == null) {
            onActiveTaskSizeChangedListeners = new ArrayList<>();
        }
        if (onActiveTaskSizeChangedListeners.contains(listener)) {
            return;
        }
        onActiveTaskSizeChangedListeners.add(listener);
    }

    public synchronized void removeOnActiveTaskSizeChangedListener(OnActiveTaskSizeChangedListener listener) {
        if (onActiveTaskSizeChangedListeners == null) {
            return;
        }
        onActiveTaskSizeChangedListeners.remove(listener);
    }

    private synchronized void notifyActiveTaskSizeChanged() {
        if (onActiveTaskSizeChangedListeners != null) {
            for (int i = onActiveTaskSizeChangedListeners.size() - 1; i >= 0; i--) {
                onActiveTaskSizeChangedListeners.get(i).onActiveTaskSizeChanged();
            }
        }
    }

    private void dispatchActiveTaskSizeChanged() {
        mMonitorHandler.sendEmptyMessage(MSG_ACTIVE_TASK_SIZE_CHANGED);
    }

    private synchronized void notifyTaskStatusChanged(long id) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskStateChanged(id);
            }
        }
    }

    private void sendTaskMsg(int what, DownloadRecord record) {
        Message message = Message.obtain(mMonitorHandler, what);
        message.obj = record;
        mMonitorHandler.sendMessage(message);
    }

    private synchronized void computeTaskSpeed(DownloadRecord record) {
        TaskEntity entity = record.entity;
        if (SpeedUtils.computeSpeed(record)) {

            Map<Integer, PartRecord> partRecords = record.partRecords;
            if (!partRecords.isEmpty()) {
                for (Map.Entry<Integer, PartRecord> entry : partRecords.entrySet()) {
                    PartRecord partRecord = entry.getValue();
                    SpeedUtils.computeSpeed(partRecord);
                }
            }

            if (downloadTaskListeners != null) {
                for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                    downloadTaskListeners.get(i).onTaskSpeedChanged(entity.getId());
                }
            }
        }
    }

    private synchronized void notifyTaskRemoved(DownloadRecord record) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskRemoved(record.entity.getId());
            }
        }
    }

    private synchronized void notifyTaskAdded(DownloadRecord record) {
        if (downloadTaskListeners != null) {
            for (int i = downloadTaskListeners.size() - 1; i >= 0; i--) {
                downloadTaskListeners.get(i).onTaskAdded(record.entity.getId());
            }
        }
    }
}

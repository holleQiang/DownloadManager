package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.listeners.DownloadTaskListeners;
import com.zhangqiang.downloadmanager.support.DownloadBundle;
import com.zhangqiang.downloadmanager.support.DownloadSupport;
import com.zhangqiang.downloadmanager.support.LocalTask;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.ftp.support.FTPDownloadSupport;
import com.zhangqiang.downloadmanager.task.http.support.HttpDownloadSupport;
import com.zhangqiang.downloadmanager.utils.LogUtils;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负责任务管理
 * 入队，出队，排队
 */
public class DownloadManager {

    public static final String TAG = DownloadManager.class.getSimpleName();

    private static final int INTERVAL_PROGRESS_SYNC = 200;
    private static final int INTERVAL_SPEED_COMPUTE = 1000;

    private static final int MSG_PROGRESS_SYNC = 1000;
    private static final int MSG_SPEED_COMPUTE = 1002;

    private static volatile DownloadManager instance;
    private int maxRunningTaskCount = 3;
    private DownloadRecord mRecordHead;
    private final AtomicInteger mTaskSize = new AtomicInteger();
    private final AtomicInteger mActiveTaskSize = new AtomicInteger();
    private final Handler mMonitorHandler;
    private final DownloadTaskListeners mDownloadTaskListeners = new DownloadTaskListeners();
    private final List<DownloadSupport> mDownloadSupportList;

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
        Context applicationContext = context.getApplicationContext();
        mDownloadSupportList = new ArrayList<>();
        mDownloadSupportList.add(new HttpDownloadSupport(applicationContext));
        mDownloadSupportList.add(new FTPDownloadSupport(applicationContext));
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
                }
                return false;
            }
        });

        DownloadRecord tail = mRecordHead;
        for (DownloadSupport downloadSupport : mDownloadSupportList) {
            List<LocalTask> localTasks = downloadSupport.loadLocalTasks();
            if (localTasks != null && !localTasks.isEmpty()) {
                for (LocalTask localTask : localTasks) {

                    DownloadBundle downloadBundle = localTask.getDownloadBundle();
                    DownloadRecord downloadRecord = makeDownloadRecord(localTask.getId(),
                            downloadSupport,
                            downloadBundle);
                    if (tail == null) {
                        tail = mRecordHead = downloadRecord;
                    } else {
                        tail.next = downloadRecord;
                        tail = downloadRecord;
                    }
                    incrementTaskSize();
                    if (localTask.isRunning()) {
                        downloadRecord.downloadTask.start();
                    }
                }
            }
        }
        tryStartIdleTask();
    }


    private DownloadRecord makeDownloadRecord(DownloadRequest request) {

        DownloadSupport target = null;
        for (DownloadSupport downloadSupport : mDownloadSupportList) {
            if(downloadSupport.support(request)){
                target = downloadSupport;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("download task cannot be null");
        }
        String id = UUID.randomUUID().toString();
        DownloadBundle downloadBundle = target.createDownloadBundle(id,request);
        if (downloadBundle == null) {
            throw new IllegalArgumentException("download task cannot be null");
        }
        return makeDownloadRecord(id,target, downloadBundle);
    }

    private DownloadRecord makeDownloadRecord(String id,
                                              DownloadSupport downloadSupport,
                                              DownloadBundle downloadBundle) {

        DownloadRecord downloadRecord = new DownloadRecord(id, downloadBundle,
                downloadSupport);
        configDownloadRecord(downloadRecord);
        return downloadRecord;
    }

    public synchronized String enqueue(DownloadRequest request) {

        DownloadRecord record = makeDownloadRecord(request);
        if (this.mRecordHead != null) {
            record.next = mRecordHead;
        }
        this.mRecordHead = record;
        incrementTaskSize();
        String taskId = record.id;
        getDownloadTaskListeners().notifyTaskAdded(taskId);
        tryStartIdleTask();
        return taskId;
    }

    public int getTaskSize() {
        return mTaskSize.get();
    }

    public synchronized void start(String id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (Objects.equals(curr.id, id)) {
                DownloadTask downloadTask = curr.downloadTask;
                if (!downloadTask.isStarted()) {
                    downloadTask.reset();
                }
            }
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    public synchronized void pause(String id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            DownloadTask downloadTask = curr.downloadTask;
            if (Objects.equals(curr.id, id)) {
                if (downloadTask.isStarted()) {
                    downloadTask.cancel();
                }
                break;
            }
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    public synchronized void deleteTask(String id, boolean deleteFile) {

        DownloadRecord prev = null;
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            DownloadTask downloadTask = curr.downloadTask;
            String taskId = curr.id;
            if (Objects.equals(id, taskId)) {

                downloadTask.cancel();

                curr.downloadSupport.handleDeleteTask(curr.downloadBundle, deleteFile);
                if (prev == null) {
                    mRecordHead = curr.next;
                } else {
                    prev.next = curr.next;
                }
                curr.next = null;
                decrementTaskSize();
                getDownloadTaskListeners().notifyTaskRemoved(taskId);
                break;
            }
            prev = curr;
            curr = curr.next;
        }
        tryStartIdleTask();
    }

    private void configDownloadRecord(final DownloadRecord record) {
        DownloadTask downloadTask = record.downloadTask;
        String id = record.id;
        downloadTask.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onReset() {
            }

            @Override
            public void onStart() {
                incrementActiveTaskSize();
            }

            @Override
            public void onComplete() {
                decrementActiveTaskSize();
                syncTaskProgress(record);
                computeTaskSpeed(record);
            }

            @Override
            public void onFail(DownloadException e) {
                e.printStackTrace();
                decrementActiveTaskSize();
                syncTaskProgress(record);
                computeTaskSpeed(record);
                LogUtils.i(TAG, "=======onFail=======");
            }

            @Override
            public void onCancel() {
                decrementActiveTaskSize();
                syncTaskProgress(record);
                computeTaskSpeed(record);
            }
        });
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

    private synchronized void tryStartIdleTask() {

        DownloadRecord curr = mRecordHead;
        while (curr != null && getActiveTaskSize() < maxRunningTaskCount) {
            DownloadTask downloadTask = curr.downloadTask;
            if (curr.downloadSupport.isTaskIdle(curr.downloadBundle)) {
                downloadTask.start();
            }
            curr = curr.next;
        }
    }

    public int getActiveTaskSize() {
        return mActiveTaskSize.get();
    }

    public synchronized TaskInfo getTaskInfo(String id) {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (Objects.equals(curr.id, id)) {
                return curr.taskInfo;
            }
            curr = curr.next;
        }
        return null;
    }

    private synchronized void syncTasksProgress() {
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            if (curr.downloadTask.isStarted()) {
                syncTaskProgress(curr);
            }
            curr = curr.next;
        }
    }

    private synchronized void syncTaskProgress(DownloadRecord record) {
        record.downloadSupport.handleProgressSync(record.downloadBundle);
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

    private void dispatchActiveTaskSizeChanged() {
        getDownloadTaskListeners().notifyActiveTaskSizeChanged();
    }

    private synchronized void computeTaskSpeed(DownloadRecord record) {

        record.downloadSupport.handleSpeedCompute(record.downloadBundle);
    }


    public void setMaxRunningTaskCount(int count) {
        if (maxRunningTaskCount != count) {
            maxRunningTaskCount = count;
            tryStartIdleTask();
        }
    }

    public DownloadTaskListeners getDownloadTaskListeners() {
        return mDownloadTaskListeners;
    }

    public List<TaskInfo> getTaskList() {
        List<TaskInfo> taskInfoList = new ArrayList<>();
        DownloadRecord curr = mRecordHead;
        while (curr != null) {
            taskInfoList.add(curr.taskInfo);
            curr = curr.next;
        }
        return taskInfoList;
    }

    private final static class DownloadRecord{

        private final String id;
        private DownloadRecord next;
        private final DownloadBundle downloadBundle;
        private final DownloadTask downloadTask;
        private final TaskInfo taskInfo;
        private final DownloadSupport downloadSupport;

        public DownloadRecord(String id,
                              DownloadBundle downloadBundle,
                              DownloadSupport downloadSupport) {
            this.id = id;
            this.downloadTask = downloadBundle.getDownloadTask();
            this.taskInfo = downloadBundle.getTaskInfo();
            this.downloadBundle = downloadBundle;
            this.downloadSupport = downloadSupport;
        }
    }
}

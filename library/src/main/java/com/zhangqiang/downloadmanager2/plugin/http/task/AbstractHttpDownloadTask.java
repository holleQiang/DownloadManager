package com.zhangqiang.downloadmanager2.plugin.http.task;

import com.zhangqiang.base.CurrentLengthOwner;
import com.zhangqiang.downloadmanager.task.speed.SpeedHelper;
import com.zhangqiang.downloadmanager.task.speed.SpeedSupport;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager2.schedule.IntervalTask;
import com.zhangqiang.downloadmanager2.schedule.Schedule;
import com.zhangqiang.downloadmanager2.task.DownloadTask;
import com.zhangqiang.downloadmanager2.task.Status;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractHttpDownloadTask extends DownloadTask implements SpeedSupport, CurrentLengthOwner {

    private final String url;
    private ResourceInfo resourceInfo;
    private long initialLength;
    private long currentLength;
    private final List<OnResourceInfoReadyListener> onResourceInfoReadyListeners = new ArrayList<>();
    private final List<OnProgressChangeListener> onProgressChangeListeners = new ArrayList<>();
    private final SpeedHelper speedHelper = new SpeedHelper(this);
    private final IntervalTask progressTask = new IntervalTask(30) {

        long lastLength = getCurrentLength();
        @Override
        public void run() {
            long currentLength = getCurrentLength();
            if(lastLength !=currentLength){
                dispatchProgressChange();
                lastLength = currentLength;
            }
        }
    };
    private final IntervalTask speedTask = new IntervalTask(3000) {
        @Override
        public void run() {
            getSpeedHelper().calculateSpeed();
        }
    };

    public AbstractHttpDownloadTask(String saveDir, String targetFileName, long createTime, String url) {
        super(saveDir, targetFileName, createTime);
        this.url = url;
    }

    public AbstractHttpDownloadTask(String saveDir, String targetFileName, long createTime, Status status, String errorMessage, String url, ResourceInfo resourceInfo, long initialLength) {
        super(saveDir, targetFileName, createTime, status, errorMessage);
        this.url = url;
        this.resourceInfo = resourceInfo;
        this.currentLength = this.initialLength = initialLength;
    }

    public void addOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.add(listener);
    }

    public void removeOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.remove(listener);
    }

    protected void dispatchResourceInfoReady(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        for (int i = onResourceInfoReadyListeners.size() - 1; i >= 0; i--) {
            onResourceInfoReadyListeners.get(i).onResourceInfoReady(resourceInfo);
        }
    }

    public void addOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListeners.add(listener);
    }

    public void removeOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListeners.remove(listener);
    }

    protected void dispatchProgressChange() {
        for (int i = onProgressChangeListeners.size() - 1; i >= 0; i--) {
            onProgressChangeListeners.get(i).onProgressChange();
        }
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    @Override
    public SpeedHelper getSpeedHelper() {
        return speedHelper;
    }


    protected void startScheduleProgress() {
        Schedule.getInstance().startSchedule(progressTask);
        Schedule.getInstance().startSchedule(speedTask);
    }

    protected void stopScheduleProgressChange() {
        Schedule.getInstance().stopSchedule(progressTask);
        Schedule.getInstance().stopSchedule(speedTask);
    }


    private void dispatchFileSaveStart() {
        startScheduleProgress();
    }

    private void dispatchFileSaveEnd() {
        stopScheduleProgressChange();
    }

    public String getUrl() {
        return url;
    }

    @Override
    public long getCurrentLength() {
        return currentLength;
    }

    private void dispatchFileSaveLength(long length) {
        this.currentLength += length;
    }

    protected void dispatchCurrentLength(long currentLength){
        this.currentLength  = currentLength;
    }

    public long getInitialLength() {
        return initialLength;
    }

    protected void performSaveFile(InputStream inputStream) throws IOException{
        try {
            File saveDir = new File(getSaveDir());
            if (!saveDir.exists() || !saveDir.isDirectory()) {
                if (!saveDir.mkdirs()) {
                    throw new IOException("create dir fail: " + saveDir.getAbsolutePath());
                }
            }
            File saveFile = new File(getSaveDir(), getSaveFileName());
            dispatchFileSaveStart();
            FileUtils.writeToFileFrom(inputStream, saveFile, getCurrentLength(), new FileUtils.WriteFileListener() {
                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    dispatchFileSaveLength(len);
                }
            });
        } finally {
            dispatchFileSaveEnd();
        }
    }
}

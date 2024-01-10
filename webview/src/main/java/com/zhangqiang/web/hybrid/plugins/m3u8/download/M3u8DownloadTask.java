package com.zhangqiang.web.hybrid.plugins.m3u8.download;

import android.text.TextUtils;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.LogCallback;
import com.arthenica.mobileffmpeg.LogMessage;
import com.arthenica.mobileffmpeg.Statistics;
import com.arthenica.mobileffmpeg.StatisticsCallback;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.web.hybrid.plugins.m3u8.download.utils.M3u8Utils;
import com.zhangqiang.web.log.WebLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class M3u8DownloadTask extends DownloadTask {

    private static final List<StatisticsCallback> statisticsCallbacks = new ArrayList<>();
    private static final List<LogCallback> logCallbacks = new ArrayList<>();
    private long totalDuration;
    private long currentDuration;

    static {
        Config.enableStatisticsCallback(new StatisticsCallback() {
            @Override
            public void apply(Statistics statistics) {
                for (StatisticsCallback callback : statisticsCallbacks) {
                    callback.apply(statistics);
                }
            }
        });
        Config.enableLogCallback(new LogCallback() {
            @Override
            public void apply(LogMessage message) {
                for (LogCallback logCallback : logCallbacks) {
                    logCallback.apply(message);
                }
            }
        });
    }

    private final String url;
    private long executionId;
    private String saveFileName;

    public M3u8DownloadTask(String url, String id, String saveDir, String targetFileName, long createTime) {
        super(id, saveDir, targetFileName, createTime);
        this.url = url;
        saveFileName = getTargetFileName();
        if (TextUtils.isEmpty(saveFileName)) {
            saveFileName = MD5Utils.getMD5(url) + ".mp4";
        }
    }

    @Override
    protected void onStart() {

        File outputFile = new File(getSaveDir(), getSaveFileName());
        if (outputFile.exists()) {
            outputFile.delete();
        }
        addCallback();
        String outFilePath = outputFile.getAbsolutePath();
        String cmd = String.format("-allowed_extensions ALL -i %s -c copy %s", url, outFilePath);
        executionId = FFmpeg.executeAsync(cmd, new ExecuteCallback() {
            @Override
            public void apply(long executionId, int returnCode) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    removeCallback();
                    dispatchSuccess();
                } else {
                    removeCallback();
                    if (getStatus() != Status.CANCELED) {
                        dispatchFail(new RuntimeException("download fail with code:" + returnCode));
                    }
                }
            }
        });
    }

    @Override
    protected void onCancel() {
        FFmpeg.cancel(executionId);
        removeCallback();
    }

    @Override
    public String getSaveFileName() {
        return saveFileName;
    }

    private final StatisticsCallback statisticsCallback = new StatisticsCallback() {
        @Override
        public void apply(Statistics statistics) {
            if (statistics.getExecutionId() == executionId) {
                WebLogger.info("===M3u8DownloadTask========" + statistics);
                dispatchCurrentLength(statistics.getSize());
            }
        }
    };

    private final LogCallback logCallback = new LogCallback() {

        private boolean nextDuration = false;

        @Override
        public void apply(LogMessage message) {
            if (message.getExecutionId() == executionId) {
                WebLogger.info("=====LogCallback======" + message.getText());
                String text = message.getText();
                if (text.contains("Duration:")) {
                    nextDuration = true;
                    return;
                }
                if (nextDuration) {
                    totalDuration = M3u8Utils.parseTime(text);
                    nextDuration = false;
                    return;
                }
                long time = M3u8Utils.getTime(text);
                if (time != -1) {
                    currentDuration = time;
                }
            }
        }
    };

    private void addCallback() {
        statisticsCallbacks.add(statisticsCallback);
        logCallbacks.add(logCallback);
    }

    private void removeCallback() {
        statisticsCallbacks.remove(statisticsCallback);
        logCallbacks.remove(logCallback);
    }


    public long getTotalDuration() {
        return totalDuration;
    }

    public long getCurrentDuration() {
        return currentDuration;
    }

    public String getUrl() {
        return url;
    }
}

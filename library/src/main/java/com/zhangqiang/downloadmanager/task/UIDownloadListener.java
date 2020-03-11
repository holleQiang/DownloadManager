package com.zhangqiang.downloadmanager.task;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

import com.zhangqiang.downloadmanager.utils.LogUtils;

public class UIDownloadListener implements DownloadListener {

    private static final String TAG = "UIDownloadListener";

    private static final int MSG_ON_START = 0;
    private static final int MSG_ON_PROGRESS = 1;
    private static final int MSG_ON_FAIL = 2;
    private static final int MSG_ON_COMPLETE = 3;
    private static final int MSG_ON_PAUSE = 4;
    private static final int MSG_SPEED_CALCULATOR = 5;
    private static final long SPEED_CALCULATE_INTERVAL = 1000;
    private boolean progressUpdating;
    private Progress pendingProgress;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ON_START:
                    onDownloadStart();
                    startSpeedCalculator();
                    LogUtils.i(TAG, "========onDownloadStart=========");
                    break;
                case MSG_ON_PROGRESS:

                    startSpeedCalculator();
                    Progress progress = (Progress) msg.obj;
                    processProgress(progress);
                    progressUpdating = false;
                    if (pendingProgress != null) {
                        processProgress(pendingProgress);
                        pendingProgress = null;
                    }
                    break;
                case MSG_ON_FAIL:
                    Throwable throwable = (Throwable) msg.obj;
                    onDownloadFail(throwable);
                    stopSpeedCalculator();
                    LogUtils.i(TAG, "========onDownloadFail=========" + throwable);
                    break;
                case MSG_ON_COMPLETE:
                    onDownloadComplete();
                    stopSpeedCalculator();
                    LogUtils.i(TAG, "========onDownloadComplete=========");
                    break;
                case MSG_ON_PAUSE:
                    onDownloadPause();
                    stopSpeedCalculator();
                    LogUtils.i(TAG, "========onDownloadPause=========");
                    break;
                case MSG_SPEED_CALCULATOR:

                    if (!speedCalculatorRunning) {
                        return;
                    }
                    long deltaLength = current - lastSpeedLength;
                    long currentTime = SystemClock.elapsedRealtime();
                    long deltaTimeMillions = currentTime - lastSpeedTime;
                    onDownloadSpeed(deltaLength, deltaTimeMillions);
                    lastSpeedLength = current;
                    lastSpeedTime = currentTime;
                    sendEmptyMessageDelayed(MSG_SPEED_CALCULATOR, SPEED_CALCULATE_INTERVAL);
                    break;
            }
        }
    };

    private void processProgress(Progress progress) {
        current = progress.current;
        long total = progress.total;
        onDownloadProgress(current, total);
        LogUtils.i(TAG, "========onDownloadProgress=========" + current + "=======" + total);
    }


    private long current;
    private boolean speedCalculatorRunning;
    private long lastSpeedLength;
    private long lastSpeedTime;

    protected void onDownloadStart() {

    }

    protected void onDownloadProgress(long current, long total) {

    }

    protected void onDownloadComplete() {

    }

    protected void onDownloadFail(Throwable e) {

    }

    protected void onDownloadPause() {

    }

    protected void onDownloadSpeed(long length, long timeMillions) {

    }

    @Override
    public final void onStart() {
        handler.sendEmptyMessage(MSG_ON_START);
    }

    @Override
    public final void onProgress(long current, long total) {


        Message message = handler.obtainMessage(MSG_ON_PROGRESS);
        Object obj = message.obj;
        if (obj == null) {
            obj = new Progress();
            message.obj = obj;
        }
        Progress progress = (Progress) obj;
        progress.current = current;
        progress.total = total;

        if (progressUpdating) {
            pendingProgress = progress;
            return;
        }
        progressUpdating = true;
        handler.sendMessage(message);
    }

    @Override
    public void onPause() {
        handler.sendEmptyMessage(MSG_ON_PAUSE);
    }

    @Override
    public final void onFail(Throwable e) {
        Message message = handler.obtainMessage(MSG_ON_FAIL);
        message.obj = e;
        handler.sendMessage(message);
    }

    @Override
    public final void onComplete() {
        handler.sendEmptyMessage(MSG_ON_COMPLETE);
    }


    private static class Progress {

        private long current;
        private long total;
    }


    private void startSpeedCalculator() {
        if (speedCalculatorRunning) {
            return;
        }
        speedCalculatorRunning = true;
        lastSpeedLength = current;
        lastSpeedTime = SystemClock.elapsedRealtime();
        handler.sendEmptyMessage(MSG_SPEED_CALCULATOR);
    }

    public void stopSpeedCalculator() {
        if (!speedCalculatorRunning) {
            return;
        }
        speedCalculatorRunning = false;
        handler.removeMessages(MSG_SPEED_CALCULATOR);
    }


}

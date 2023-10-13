package com.zhangqiang.downloadmanager.task.interceptor.fail;

import android.os.Handler;
import android.os.Looper;

import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class RetryFailInterceptor implements FailInterceptor {

    public static final String TAG = RetryFailInterceptor.class.getSimpleName();

    private final AtomicInteger retryCount = new AtomicInteger(0);
    private final DownloadTask downloadTask;
    private final Handler handler = new Handler(Looper.getMainLooper());

    public RetryFailInterceptor(DownloadTask downloadTask) {
        this.downloadTask = downloadTask;
    }

    private final OnStatusChangeListener onStatusChangeListener = new OnStatusChangeListener() {
        @Override
        public void onStatusChange(Status newStatus, Status oldStatus) {
            if (newStatus == Status.CANCELED) {
                handler.removeCallbacks(forceStartTask);
                retryCount.set(0);
            }
            downloadTask.removeStatusChangeListener(this);
        }
    };

    private final Runnable forceStartTask = new Runnable() {
        @Override
        public void run() {
            Status status = downloadTask.getStatus();
            if (status == Status.CANCELED || status == Status.FAIL) {
                return;
            }
            if (status != Status.DOWNLOADING) {
                throw new IllegalStateException("bug may exists:" + status);
            }
            downloadTask.forceStart();
            int count = retryCount.incrementAndGet();
            LogUtils.i(TAG, "重试第：" + count + "次");
            //监听取消任务时，也取消重试任务
            downloadTask.addStatusChangeListener(onStatusChangeListener);
        }
    };

    @Override
    public void onIntercept(FailChain chain) {
        downloadTask.removeStatusChangeListener(onStatusChangeListener);
        int count = retryCount.get();
        if (count == 0) {
            downloadTask.addStatusChangeListener(onStatusChangeListener);
        }
        LogUtils.i(TAG, "onIntercept======retryCount====" + retryCount);
        if (count < 2) {
            handler.postDelayed(forceStartTask, 2000);
        } else if (count < 5) {
            handler.postDelayed(forceStartTask, 5000);
        } else {
            chain.proceed(chain.getThrowable());
            retryCount.set(0);
        }
    }
}

package com.zhangqiang.downloadmanager.task.interceptor.fail;

import android.os.Handler;
import android.os.Looper;

import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;

public class RetryFailInterceptor implements FailInterceptor {

    public static final String TAG = RetryFailInterceptor.class.getSimpleName();

    private int retryCount = 0;
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
                retryCount = 0;
            }
            downloadTask.removeStatusChangeListener(this);
        }
    };

    private final Runnable forceStartTask = new Runnable() {
        @Override
        public void run() {
            if (downloadTask.getStatus() == Status.CANCELED) {
                return;
            }
            LogUtils.i(TAG, "重试第：" + retryCount + "次");
            downloadTask.forceStart();
            //监听取消任务时，也取消重试任务
            downloadTask.addStatusChangeListener(onStatusChangeListener);
        }
    };

    @Override
    public void onIntercept(FailChain chain) {
        retryCount++;
        if (retryCount <= 2) {
            handler.postDelayed(forceStartTask, 2000);
        } else if (retryCount <= 5) {
            handler.postDelayed(forceStartTask, 5000);
        } else {
            chain.proceed(chain.getThrowable());
            retryCount = 0;
        }
    }
}

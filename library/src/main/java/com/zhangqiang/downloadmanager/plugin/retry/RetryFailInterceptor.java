package com.zhangqiang.downloadmanager.plugin.retry;

import android.os.Handler;
import android.os.Looper;

import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.task.interceptor.fail.FailChain;
import com.zhangqiang.downloadmanager.task.interceptor.fail.FailInterceptor;
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
                //重试被暂停
                handler.removeCallbacks(forceStartTask);
                retryCount.set(0);
                downloadTask.removeStatusChangeListener(this);
            }else if(newStatus == Status.SUCCESS){
                //重试成功
                handler.removeCallbacks(forceStartTask);
                retryCount.set(0);
                downloadTask.removeStatusChangeListener(this);
            }else {
                //只为了监听重试暂停和成功行为，如果有别的case，可能是有bug
                throw new IllegalStateException("bug may exists:" + newStatus);
            }
        }
    };

    private final Runnable forceStartTask = new Runnable() {
        @Override
        public void run() {
            int count = retryCount.incrementAndGet();
            LogUtils.i(TAG, "重试第：" + count + "次"+",downloadTask.Status:"+downloadTask.getStatus());
            downloadTask.forceStart();
        }
    };

    @Override
    public void onIntercept(FailChain chain) {

        chain.getThrowable().printStackTrace();

        //添加监听
        downloadTask.addStatusChangeListener(onStatusChangeListener);
        //移除pending的任务
        handler.removeCallbacksAndMessages(forceStartTask);
        int count = retryCount.get();
        if (count < 2) {
            handler.postDelayed(forceStartTask, 1000);
        } else if (count < 5) {
            handler.postDelayed(forceStartTask, 1000);
        } else {
            //重试结束，抛出异常
            //移除监听
            downloadTask.removeStatusChangeListener(onStatusChangeListener);
            //重置重试次数
            retryCount.set(0);
            //抛出异常
            chain.proceed(chain.getThrowable());
        }
    }
}

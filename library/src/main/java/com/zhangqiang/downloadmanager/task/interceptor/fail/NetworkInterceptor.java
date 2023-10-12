package com.zhangqiang.downloadmanager.task.interceptor.fail;

import android.content.Context;

import com.zhangqiang.downloadmanager.manager.network.NetWorkManager;
import com.zhangqiang.downloadmanager.manager.network.OnAvailableChangedListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;

public class NetworkInterceptor implements FailInterceptor{

    private final Context context;
    private final DownloadTask downloadTask;

    public NetworkInterceptor(Context context, DownloadTask downloadTask) {
        this.context = context;
        this.downloadTask = downloadTask;
    }

    private final OnAvailableChangedListener onAvailableChangedListener = new OnAvailableChangedListener() {
        @Override
        public void onAvailableChanged(boolean available) {
            if(available){
                downloadTask.start();
            }
        }
    };

    @Override
    public void onIntercept(FailChain chain) {
        chain.proceed(chain.getThrowable());
        if(downloadTask.getStatus() == Status.FAIL){
            downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
                @Override
                public void onStatusChange(Status newStatus, Status oldStatus) {
                    if(newStatus == Status.DOWNLOADING){
                        NetWorkManager.getInstance(context).removeOnAvailableChangedListener(onAvailableChangedListener);
                    }
                }
            });
            NetWorkManager.getInstance(context).addOnAvailableChangedListener(onAvailableChangedListener);
        }
    }
}

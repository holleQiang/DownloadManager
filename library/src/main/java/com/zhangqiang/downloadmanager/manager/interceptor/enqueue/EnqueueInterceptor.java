package com.zhangqiang.downloadmanager.manager.interceptor.enqueue;

import com.zhangqiang.downloadmanager.task.DownloadTask;

public interface EnqueueInterceptor {

    DownloadTask onIntercept(Chain chain);
}

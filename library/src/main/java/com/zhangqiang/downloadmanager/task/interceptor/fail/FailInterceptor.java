package com.zhangqiang.downloadmanager.task.interceptor.fail;

public interface FailInterceptor {

    void onIntercept(FailChain chain);
}

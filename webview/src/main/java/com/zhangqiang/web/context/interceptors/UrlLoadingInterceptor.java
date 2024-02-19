package com.zhangqiang.web.context.interceptors;

public interface UrlLoadingInterceptor {

    boolean onInterceptUrlLoading(Chain chain);
}

package com.zhangqiang.downloadmanager.task.http.engine;

public interface HttpEngine {

    Cancelable get(HttpRequest request,Callback callback);
}

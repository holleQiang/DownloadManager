package com.zhangqiang.downloadmanager.task.http.engine;

public interface Callback {

    void onResponse(HttpResponse response);

    void onFail(Throwable e);
}

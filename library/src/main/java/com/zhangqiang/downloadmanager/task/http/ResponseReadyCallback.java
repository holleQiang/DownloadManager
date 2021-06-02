package com.zhangqiang.downloadmanager.task.http;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-01
 */
public interface ResponseReadyCallback {

    void onResponseReady(HttpResponse httpResponse);
}

package com.zhangqiang.downloadmanager.task.http.okhttp;

import com.zhangqiang.downloadmanager.task.http.FiledSetter;

import okhttp3.Request;

public class OkHttpFiledSetter implements FiledSetter {

    private Request.Builder builder;

    public OkHttpFiledSetter(Request.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void setField(String key, String value) {
        builder.removeHeader(key);
        builder.addHeader(key, value);
    }
}

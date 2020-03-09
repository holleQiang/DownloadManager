package com.zhangqiang.downloadmanager.task.http;

import com.zhangqiang.downloadmanager.utils.HttpUtils;

import okhttp3.Request;

public class OkHttpRequestPropertySetter implements HttpUtils.RequestPropertySetter {

    private Request.Builder builder;

    public OkHttpRequestPropertySetter(Request.Builder builder) {
        this.builder = builder;
    }

    @Override
    public void setRequestProperty(String key, String value) {
        builder.removeHeader(key);
        builder.addHeader(key, value);
    }
}

package com.zhangqiang.web.context.interceptors;

public abstract class Chain {

    private final String url;

    public Chain(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public abstract boolean proceed(String url);
}

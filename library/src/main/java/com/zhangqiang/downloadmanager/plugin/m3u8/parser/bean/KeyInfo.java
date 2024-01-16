package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

public class KeyInfo {
    private final String method;
    private final String uri;

    public KeyInfo(String method, String uri) {
        this.method = method;
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return "KeyInfo{" +
                "method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}

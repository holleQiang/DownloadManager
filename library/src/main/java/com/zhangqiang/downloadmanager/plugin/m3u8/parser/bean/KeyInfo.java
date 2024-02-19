package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

public class KeyInfo {
    private final String method;
    private final String uri;
    private final String iv;

    public KeyInfo(String method, String uri, String iv) {
        this.method = method;
        this.uri = uri;
        this.iv = iv;
    }

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public String getIV() {
        return iv;
    }

    @Override
    public String toString() {
        return "KeyInfo{" +
                "method='" + method + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}

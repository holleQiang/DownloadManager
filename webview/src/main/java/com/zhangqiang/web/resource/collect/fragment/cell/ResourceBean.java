package com.zhangqiang.web.resource.collect.fragment.cell;

public class ResourceBean {

    private final String url;
    private final String title;

    public ResourceBean(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}

package com.zhangqiang.web.boomark.bean;

import java.util.List;

public class BookMarkBean {

    private String id;
    private String url;
    private String title;
    private String iconUrl;
    private List<BookMarkBean> children;
    private BookMarkBean parent;

    public String getId() {
        return id;
    }

    public BookMarkBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public BookMarkBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public BookMarkBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<BookMarkBean> getChildren() {
        return children;
    }

    public BookMarkBean setChildren(List<BookMarkBean> children) {
        this.children = children;
        return this;
    }

    public BookMarkBean getParent() {
        return parent;
    }

    public BookMarkBean setParent(BookMarkBean parent) {
        this.parent = parent;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public BookMarkBean setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }
}

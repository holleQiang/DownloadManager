package com.zhangqiang.web.resource.collect.dialog;

public class TabFeedBean {

    private final String tabTitle;
    private final int category;

    public TabFeedBean(String tabTitle, int category) {
        this.tabTitle = tabTitle;
        this.category = category;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public int getCategory() {
        return category;
    }
}

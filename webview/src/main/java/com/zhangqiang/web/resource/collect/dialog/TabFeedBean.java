package com.zhangqiang.web.resource.collect.dialog;

public class TabFeedBean {

    private final String tabTitle;
    private final int providerId;

    public TabFeedBean(String tabTitle, int providerId) {
        this.tabTitle = tabTitle;
        this.providerId = providerId;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public int getProviderId() {
        return providerId;
    }
}

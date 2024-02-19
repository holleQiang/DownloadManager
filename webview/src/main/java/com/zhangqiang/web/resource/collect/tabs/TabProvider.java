package com.zhangqiang.web.resource.collect.tabs;

import com.zhangqiang.web.resource.collect.bean.WebResource;

public interface TabProvider {

    int getId();

    String getTabTitle();

    boolean isTargetResource(WebResource webResource);
}

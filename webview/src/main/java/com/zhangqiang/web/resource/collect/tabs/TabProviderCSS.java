package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

public class TabProviderCSS implements TabProvider{
    private final Context context;

    public TabProviderCSS(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.css);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        return Utils.isCSSUrl(webResource.getUrl());
    }
}

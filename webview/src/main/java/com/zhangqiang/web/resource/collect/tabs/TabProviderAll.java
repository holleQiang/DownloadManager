package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.webview.R;

public class TabProviderAll implements TabProvider{

    private final Context context;

    public TabProviderAll(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.all);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        return true;
    }
}

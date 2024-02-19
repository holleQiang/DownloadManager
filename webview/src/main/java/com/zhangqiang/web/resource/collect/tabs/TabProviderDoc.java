package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.webview.R;

public class TabProviderDoc implements TabProvider{

    private final Context context;

    public TabProviderDoc(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.doc);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        String url = webResource.getUrl();
        return url.contains(".txt") || url.contains(".doc");
    }
}

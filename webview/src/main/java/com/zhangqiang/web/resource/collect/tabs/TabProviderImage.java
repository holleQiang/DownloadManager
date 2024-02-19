package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

public class TabProviderImage implements TabProvider{

    private Context context;

    public TabProviderImage(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.image);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        return Utils.isImageUrl(webResource.getUrl());
    }
}

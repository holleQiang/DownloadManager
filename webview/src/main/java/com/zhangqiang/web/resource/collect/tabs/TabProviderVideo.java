package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

public class TabProviderVideo implements TabProvider{

    private final Context context;

    public TabProviderVideo(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.video);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        return Utils.isVideoUrl(webResource.getUrl());
    }
}

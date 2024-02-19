package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

public class TabProviderAudio implements TabProvider{

    private final Context context;

    public TabProviderAudio(Context context) {
        this.context = context;
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String getTabTitle() {
        return context.getString(R.string.music);
    }

    @Override
    public boolean isTargetResource(WebResource webResource) {
        return Utils.isAudioUrl(webResource.getUrl());
    }
}

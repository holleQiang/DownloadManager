package com.zhangqiang.web.history;

import android.graphics.Bitmap;

import com.zhangqiang.web.activity.OnLoadUrlListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnReceiveIconListener;
import com.zhangqiang.web.context.OnReceiveTitleListener;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.service.VisitRecordService;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

public class VisitRecordPlugin implements WebPlugin {
    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnLoadUrlListener(new OnLoadUrlListener() {
                    @Override
                    public void onLoadUrl(String url) {
                        new VisitRecordService(webContext.getActivity()).add(url);
                    }
                });
                webContext.addOnReceiveTitleListener(new OnReceiveTitleListener() {
                    @Override
                    public void onReceiveTitle(String title) {
                        String url = webContext.getWebView().getUrl();
                        new VisitRecordService(webContext.getActivity()).updateTitle(url,title);
                    }
                });
                webContext.addOnReceiveIconListener(new OnReceiveIconListener() {
                    @Override
                    public void onReceiveIcon(Bitmap bitmap) {
                        String url = webContext.getWebView().getUrl();
                        new VisitRecordService(webContext.getActivity()).updateIcon(url,bitmap);
                    }
                });
            }
        });
    }
}

package com.zhangqiang.web.history;

import android.graphics.Bitmap;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.OnActivityDestroyListener;
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

    private VisitRecordService visitRecordService;

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnActivityCreatedListener(new OnActivityCreatedListener() {
                    @Override
                    public void onActivityCreated(FragmentActivity activity) {
                        visitRecordService = new VisitRecordService(activity.getApplicationContext());
                    }
                });
                webContext.addOnActivityDestroyListener(new OnActivityDestroyListener() {
                    @Override
                    public void onActivityDestroy() {
                        visitRecordService = null;
                    }
                });
                webContext.addOnLoadUrlListener(new OnLoadUrlListener() {
                    @Override
                    public void onLoadUrl(String url) {
                        visitRecordService.add(url);
                    }
                });
                webContext.addOnReceiveTitleListener(new OnReceiveTitleListener() {
                    @Override
                    public void onReceiveTitle(String title) {
                        String url = webContext.getWebView().getUrl();
                        visitRecordService.updateTitle(url, title);
                    }
                });
                webContext.addOnReceiveIconListener(new OnReceiveIconListener() {
                    @Override
                    public void onReceiveIcon(Bitmap bitmap) {
                        String url = webContext.getWebView().getUrl();
                        visitRecordService.updateIcon(url, bitmap);
                    }
                });
            }
        });
    }
}

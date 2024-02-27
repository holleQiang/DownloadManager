package com.zhangqiang.web.history;

import android.graphics.Bitmap;
import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.OnActivityDestroyListener;
import com.zhangqiang.web.activity.OnLoadUrlListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnReceiveIconListener;
import com.zhangqiang.web.context.OnReceiveTitleListener;
import com.zhangqiang.web.context.PageLoadListener;
import com.zhangqiang.web.context.interceptors.Chain;
import com.zhangqiang.web.context.interceptors.UrlLoadingInterceptor;
import com.zhangqiang.web.history.service.VisitRecordService;
import com.zhangqiang.web.hybrid.methods.GetIconMethod;
import com.zhangqiang.web.hybrid.plugins.JSCallPlugin;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;
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
                        visitRecordService.save(url);
                    }
                });
                webContext.addOnReceiveTitleListener(new OnReceiveTitleListener() {
                    @Override
                    public void onReceiveTitle(String title) {
                        String url = webContext.getWebView().getUrl();
                        visitRecordService.updateTitle(url, title);
                    }
                });
                webContext.addPageLoadListener(new PageLoadListener() {
                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {

                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        JSCallPlugin jsCallPlugin = (JSCallPlugin) WebManager.getInstance().findPluginOrThrow(new WebManager.Filter() {
                            @Override
                            public boolean onFilter(WebPlugin plugin) {
                                return plugin instanceof JSCallPlugin;
                            }
                        });
                        jsCallPlugin.invokeHybridMethod(new GetIconMethod().setCallback(new GetIconMethod.Callback() {
                            @Override
                            public void onGetIconUrl(String iconUrl) {
                                visitRecordService.updateIcon(url, iconUrl);
                            }
                        }));
                    }
                });
                webContext.addUrlLoadingInterceptor(new UrlLoadingInterceptor() {
                    @Override
                    public boolean onInterceptUrlLoading(Chain chain) {
                        String url = chain.getUrl();
                        visitRecordService.save(url);
                        return false;
                    }
                });
            }
        });
    }
}

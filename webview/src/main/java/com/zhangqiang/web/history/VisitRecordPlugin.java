package com.zhangqiang.web.history;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.webkit.WebView;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.OnActivityDestroyListener;
import com.zhangqiang.web.activity.OnLoadUrlListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.WebViewActivity;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.context.OnReceiveTitleListener;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.PageLoadListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.context.interceptors.Chain;
import com.zhangqiang.web.context.interceptors.UrlLoadingInterceptor;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.dialog.VisitRecordDialog;
import com.zhangqiang.web.history.service.VisitRecordService;
import com.zhangqiang.web.hybrid.methods.GetIconMethod;
import com.zhangqiang.web.hybrid.plugins.JSCallPlugin;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.utils.URLUtils;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

public class VisitRecordPlugin implements WebPlugin {

    private static final int MENU_ID_VISIT_RECORD = 5;
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
                        if (URLUtils.isHttpUrl(url)) {
                            visitRecordService.save(url);
                        }
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
                        if (URLUtils.isHttpUrl(url)) {
                            visitRecordService.save(url);
                        }
                        return chain.proceed(url);
                    }
                });
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean().setId(MENU_ID_VISIT_RECORD).setTitle(webContext.getActivity().getString(R.string.visit_record)));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == MENU_ID_VISIT_RECORD) {

                            VisitRecordDialog dialog = VisitRecordDialog.newInstance(webContext.getSessionId());
                            dialog.show(webContext.getActivity().getSupportFragmentManager(), "visit_record_dialog");
                        }
                    }
                });
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(State state, State oldState) {
                        if (state == State.WEB_VIEW_CREATE) {

                            String url = webContext.getUrl();
                            if (TextUtils.isEmpty(url)) {

                                VisitRecordBean lastVisitRecord = visitRecordService.getLastVisitRecord();
                                WebViewActivity activity = webContext.getActivity();
                                if (activity != null) {
                                    activity.performSearch(lastVisitRecord.getUrl());
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}

package com.zhangqiang.web.resource.collect;

import android.webkit.WebView;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.context.OnLoadResourceListener;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

import java.util.ArrayList;
import java.util.List;

public class ResourceCollectPlugin implements WebPlugin {

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnLoadResourceListener(new OnLoadResourceListener() {
                    @Override
                    public void onLoadResource(WebView view, String url) {

                    }
                });
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenus() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean().setTitle("资源管理器"));
                        return menuItemBeans;
                    }
                });
            }
        });
    }
}

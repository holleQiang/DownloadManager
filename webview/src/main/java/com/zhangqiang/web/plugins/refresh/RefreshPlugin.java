package com.zhangqiang.web.plugins.refresh;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

import java.util.ArrayList;
import java.util.List;

public class RefreshPlugin implements WebPlugin {
    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean().setTitle("刷新").setId(1));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == 1) {
                            webContext.getWebView().reload();
                        }
                    }
                });
            }
        });
    }
}

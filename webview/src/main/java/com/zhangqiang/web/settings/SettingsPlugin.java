package com.zhangqiang.web.settings;

import android.content.Intent;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.WebViewActivity;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

public class SettingsPlugin implements WebPlugin {

    public static final int MENU_ID_SETTINGS = 9;

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean().setTitle(webContext.getActivity().getString(R.string.settings)).setId(MENU_ID_SETTINGS));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == MENU_ID_SETTINGS) {
                            WebViewActivity activity = webContext.getActivity();
                            activity.startActivity(new Intent(activity, WebViewSettingsActivity.class));
                        }
                    }
                });
            }
        });
    }
}

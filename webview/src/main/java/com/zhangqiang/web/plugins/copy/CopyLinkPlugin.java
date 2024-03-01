package com.zhangqiang.web.plugins.copy;

import android.widget.Toast;

import com.zhangqiang.common.utils.ClipboardUtils;
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

public class CopyLinkPlugin implements WebPlugin {
    private static final int MENU_ID_COPY_LINK = 6;

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addMenuProvider(new MenuProvider() {

                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean()
                                .setTitle(webContext.getActivity().getString(R.string.copy_link))
                                .setId(MENU_ID_COPY_LINK));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == MENU_ID_COPY_LINK) {
                            WebViewActivity activity = webContext.getActivity();
                            ClipboardUtils.copy(webContext.getActivity(), webContext.getWebView().getUrl());
                            Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}

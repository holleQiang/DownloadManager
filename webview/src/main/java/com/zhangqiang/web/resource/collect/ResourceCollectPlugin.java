package com.zhangqiang.web.resource.collect;

import android.webkit.WebView;

import com.zhangqiang.web.activity.OnActivityDestroyListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.context.OnLoadResourceListener;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.dialog.ResourceLookupDialog;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceCollectPlugin implements WebPlugin {

    private final Map<String, List<WebResource>> resourceMap = new ConcurrentHashMap<>();

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
                                .setTitle(webContext.getActivity().getString(R.string.resource_manager))
                                .setId(2));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == 2) {
                            ResourceLookupDialog.newInstance(webContext.getSessionId())
                                    .show(webContext.getActivity().getSupportFragmentManager(), "resource");
                        }
                    }
                });
                webContext.addOnLoadResourceListener(new OnLoadResourceListener() {
                    @Override
                    public void onLoadResource(WebView view, String url) {
                        List<WebResource> resourceList = resourceMap.get(webContext.getSessionId());
                        if (resourceList == null) {
                            resourceList = new ArrayList<>();
                            resourceMap.put(webContext.getSessionId(), resourceList);
                        }
                        resourceList.add(new WebResource().setUrl(url));
                    }
                });
                webContext.addOnActivityDestroyListener(new OnActivityDestroyListener() {
                    @Override
                    public void onActivityDestroy() {
                        resourceMap.remove(webContext.getSessionId());
                    }
                });
            }
        });
    }

    public List<WebResource> getResourceList(String sessionId) {
        return resourceMap.get(sessionId);
    }
}

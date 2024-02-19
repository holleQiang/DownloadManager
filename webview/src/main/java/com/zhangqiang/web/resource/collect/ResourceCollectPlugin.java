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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceCollectPlugin implements WebPlugin {

    private final Map<String, List<WebResource>> resourceMap = new ConcurrentHashMap<>();
    private final List<OnResourceChangeListener> onResourceChangeListeners = new ArrayList<>();

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
                        WebResource webResource = new WebResource().setUrl(url);
                        resourceList.add(webResource);

                        dispatchWebResourceLoad(webResource, webContext);
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

    private synchronized void dispatchWebResourceLoad(WebResource webResource, WebActivityContext webContext) {
        for (int i = onResourceChangeListeners.size() - 1; i >= 0; i--) {
            onResourceChangeListeners.get(i).onLoadWebResource(webContext.getSessionId(), webResource);
        }
    }

    public List<WebResource> getResourceList(String sessionId) {
        return resourceMap.get(sessionId);
    }

    public synchronized void addOnResourceChangeListener(OnResourceChangeListener listener) {
        if (onResourceChangeListeners.contains(listener)) {
            return;
        }
        onResourceChangeListeners.add(listener);
    }

    public synchronized void removeOnResourceChangeListener(OnResourceChangeListener listener) {
        onResourceChangeListeners.remove(listener);
    }
}

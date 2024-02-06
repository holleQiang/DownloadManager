package com.zhangqiang.web.activity;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.context.WebContext;

import java.util.ArrayList;
import java.util.List;

public class WebActivityContext extends WebContext {

    public FragmentActivity activity;
    private final List<OnActivityCreatedListener> onActivityCreatedListeners = new ArrayList<>();
    private final List<OnActivityDestroyListener> onActivityDestroyListeners = new ArrayList<>();
    private final List<OnLoadUrlListener> onLoadUrlListeners = new ArrayList<>();
    private final List<MenuProvider> menuProviders = new ArrayList<>();

    public WebActivityContext(String sessionId, String url) {
        super(sessionId, url);
    }

    public void dispatchActivityCreate(FragmentActivity activity) {
        this.activity = activity;
        for (int i = onActivityCreatedListeners.size() - 1; i >= 0; i--) {
            onActivityCreatedListeners.get(i).onActivityCreated(activity);
        }
    }

    public void dispatchActivityDestroy() {
        this.activity = null;
        for (int i = onActivityDestroyListeners.size() - 1; i >= 0; i--) {
            onActivityDestroyListeners.get(i).onActivityDestroy();
        }
    }

    public FragmentActivity getActivity() {
        return activity;
    }

    public void addOnActivityCreatedListener(OnActivityCreatedListener listener) {
        if (onActivityCreatedListeners.contains(listener)) {
            return;
        }
        onActivityCreatedListeners.add(listener);
    }

    public void removeOnActivityCreatedListener(OnActivityCreatedListener listener) {
        onActivityCreatedListeners.remove(listener);
    }

    public void addOnActivityDestroyListener(OnActivityDestroyListener listener) {
        if (onActivityDestroyListeners.contains(listener)) {
            return;
        }
        onActivityDestroyListeners.add(listener);
    }

    public void removeOnActivityDestroyListener(OnActivityDestroyListener listener) {
        onActivityDestroyListeners.remove(listener);
    }

    public void addOnLoadUrlListener(OnLoadUrlListener listener) {
        if (onLoadUrlListeners.contains(listener)) {
            return;
        }
        onLoadUrlListeners.add(listener);
    }

    public void removeOnLoadOnLoadUrlListener(OnLoadUrlListener listener) {
        onLoadUrlListeners.remove(listener);
    }

    public void dispatchLoadUrl(String url) {
        for (int i = onLoadUrlListeners.size() - 1; i >= 0; i--) {
            onLoadUrlListeners.get(i).onLoadUrl(url);
        }
    }


    public void addMenuProvider(MenuProvider provider) {
        if (menuProviders.contains(provider)) {
            return;
        }
        menuProviders.add(provider);
    }

    public void removeMenuProvider(MenuProvider provider) {
        menuProviders.remove(provider);
    }

    public List<MenuItemBean> getMenus() {
        List<MenuItemBean> menuItems = new ArrayList<>();
        for (MenuProvider menuProvider : menuProviders) {
            List<MenuItemBean> tempMenuItems = menuProvider.provideMenus();
            if (tempMenuItems != null) {
                menuItems.addAll(tempMenuItems);
            }
        }
        return menuItems;
    }

}

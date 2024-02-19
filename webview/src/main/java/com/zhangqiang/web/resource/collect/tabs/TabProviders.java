package com.zhangqiang.web.resource.collect.tabs;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class TabProviders {

    private final Context context;
    private static volatile TabProviders instance = null;
    private final List<TabProvider> tabProviders = new ArrayList<>();

    public TabProviders(Context context) {
        this.context = context.getApplicationContext();
        addProviders();
    }

    private void addProviders() {
        addProvider(new TabProviderAll(context));
        addProvider(new TabProviderImage(context));
        addProvider(new TabProviderVideo(context));
        addProvider(new TabProviderDoc(context));
        addProvider(new TabProviderAudio(context));
        addProvider(new TabProviderCSS(context));
    }

    public static TabProviders get(Context context) {
        if (instance == null) {
            synchronized (TabProviders.class) {
                if (instance == null) {
                    instance = new TabProviders(context);
                }
            }
        }
        return instance;
    }

    public List<TabProvider> getTabProviders() {
        return tabProviders;
    }

    public TabProvider findById(int id) {
        for (TabProvider tabProvider : tabProviders) {
            if (tabProvider.getId() == id) {
                return tabProvider;
            }
        }
        return null;
    }

    private void addProvider(TabProvider tabProvider) {
        int id = tabProvider.getId();
        TabProvider provider = findById(id);
        if (provider != null) {
            throw new IllegalArgumentException("provider id has already exists");
        }
        tabProviders.add(tabProvider);
    }
}

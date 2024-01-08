package com.zhangqiang.web.plugin;

import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;

import java.util.List;

public class PluginContext {

    private final WebManager webManager;

    public PluginContext(WebManager webManager) {
        this.webManager = webManager;
    }

    public void addOnOpenWebViewActivityListener(OnOpenWebViewActivityListener onOpenWebViewActivityListener) {
        webManager.addOnOpenWebViewActivityListener(onOpenWebViewActivityListener);
    }

    public List<WebPlugin> findPlugins(WebManager.Filter filter) {
        return webManager.findPlugins(filter);
    }
}

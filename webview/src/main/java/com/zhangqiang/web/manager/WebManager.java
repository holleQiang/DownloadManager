package com.zhangqiang.web.manager;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.WebViewActivity;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.history.VisitRecordPlugin;
import com.zhangqiang.web.hybrid.plugins.HybridPlugin;
import com.zhangqiang.web.hybrid.plugins.JSCallPlugin;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.resource.collect.ResourceCollectPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebManager {

    private static final WebManager instance = new WebManager();
    private final List<WebContext> webContexts = new ArrayList<>();
    private final List<WebPlugin> webPlugins = new ArrayList<>();
    private final List<OnOpenWebViewActivityListener> onOpenWebViewActivityListeners = new ArrayList<>();

    public static WebManager getInstance() {
        return instance;
    }

    private WebManager() {
        registerPlugin(new HybridPlugin());
        registerPlugin(new JSCallPlugin());
        registerPlugin(new VisitRecordPlugin());
        registerPlugin(new ResourceCollectPlugin());
    }

    public void applyPlugins() {
        PluginContext pluginContext = new PluginContext(this);
        for (WebPlugin webPlugin : webPlugins) {
            webPlugin.apply(pluginContext);
        }
    }

    public interface Filter {
        boolean onFilter(WebPlugin plugin);
    }

    public List<WebPlugin> findPlugins(Filter filter) {
        List<WebPlugin> targets = new ArrayList<>();
        for (WebPlugin webPlugin : webPlugins) {
            if (filter.onFilter(webPlugin)) {
                targets.add(webPlugin);
            }
        }
        return targets;
    }

    public void openWebViewActivity(Context context, String url) {
        openWebViewActivity(context, url, new OpenOptions.Builder().setNewTask(true).build());
    }

    public void openWebViewActivity(Context context, String url, OpenOptions options) {
        WebActivityContext webContext = createWebActivityContext(url);
        dispatchOpenWebViewActivity(webContext);
        webContext.dispatchOpenStart();
        try {
            String id = webContext.getSessionId();
            Intent intent = WebViewActivity.newIntent(context, id);
            if (options != null && options.isNewTask()) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
            webContext.dispatchOpenSuccess();
        } catch (Throwable e) {
            webContext.dispatchOpenFail(e);
        }
    }

    public WebContext getWebContext(String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        for (WebContext webContext : webContexts) {
            if (webContext.getSessionId().equals(sessionId)) {
                return webContext;
            }
        }
        return null;
    }

    public WebActivityContext fromActivityRestore(String sessionId, String url) {
        return createWebActivityContext(sessionId, url);
    }

    public void registerPlugin(WebPlugin webPlugin) {
        webPlugins.add(webPlugin);
    }

    public void addOnOpenWebViewActivityListener(OnOpenWebViewActivityListener listener) {
        onOpenWebViewActivityListeners.add(listener);
    }

    public void removeOnOpenWebViewActivityListener(OnOpenWebViewActivityListener listener) {
        onOpenWebViewActivityListeners.remove(listener);
    }

    private void dispatchOpenWebViewActivity(WebActivityContext webContext) {
        for (int i = onOpenWebViewActivityListeners.size() - 1; i >= 0; i--) {
            onOpenWebViewActivityListeners.get(i).onOpenWebActivity(webContext);
        }
    }

    private String generateWebId() {
        return UUID.randomUUID().toString();
    }

    private WebActivityContext createWebActivityContext(String url) {
        return createWebActivityContext(generateWebId(), url);
    }

    private WebActivityContext createWebActivityContext(String sessionId, String url) {
        WebActivityContext webContext = new WebActivityContext(sessionId, url);
        webContext.addOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(State state, State oldState) {
                if (state == State.WEB_VIEW_DESTROY) {
                    for (int i = 0; i < webContexts.size(); i++) {
                        WebContext context = webContexts.get(i);
                        if (context.getSessionId().equals(sessionId)) {
                            webContexts.remove(i);
                            break;
                        }
                    }
                }
            }
        });
        webContext.addOnActivityCreatedListener(new OnActivityCreatedListener() {
            @Override
            public void onActivityCreated(FragmentActivity activity) {

            }
        });
        webContexts.add(webContext);
        return webContext;
    }
}

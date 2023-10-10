package com.zhangqiang.web.manager;

import android.app.Activity;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.WebViewActivity;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.hybrid.HybridPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WebManager {

    private static final WebManager instance = new WebManager();
    private final Map<String, WebContext> webContextMap = new HashMap<>();
    private final List<WebPlugin> webPlugins = new ArrayList<>();
    private final List<OnOpenWebViewActivityListener> onOpenWebViewActivityListeners = new ArrayList<>();

    public static WebManager getInstance() {
        return instance;
    }

    private WebManager() {
        registerPlugin(new HybridPlugin());
    }

    public void openWebViewActivity(Context context, String url) {
        WebActivityContext webContext = new WebActivityContext(url);
        String id = UUID.randomUUID().toString();
        webContext.addOnStateChangeListener(new OnStateChangeListener() {
            @Override
            public void onStateChange(State state, State oldState) {
                if (state == State.WEB_VIEW_DESTROY) {
                    webContextMap.remove(id);
                }
            }
        });
        webContext.addOnActivityCreatedListener(new OnActivityCreatedListener() {
            @Override
            public void onActivityCreated(FragmentActivity activity) {

            }
        });
        dispatchOpenWebViewActivity(webContext);
        webContext.dispatchOpenStart();
        try {
            WebViewActivity.open(context, url, id);
            webContextMap.put(id, webContext);
            webContext.dispatchOpenSuccess();
        } catch (Throwable e) {
            webContext.dispatchOpenFail(e);
        }
    }

    public WebContext getWebContext(String id) {
        return webContextMap.get(id);
    }

    public void registerPlugin(WebPlugin webPlugin) {
        webPlugins.add(webPlugin);
        webPlugin.apply(this);
    }

    public void addOnOpenWebViewActivityListener(OnOpenWebViewActivityListener listener){
        onOpenWebViewActivityListeners.add(listener);
    }

    public void removeOnOpenWebViewActivityListener(OnOpenWebViewActivityListener listener){
        onOpenWebViewActivityListeners.remove(listener);
    }

    private void dispatchOpenWebViewActivity(WebActivityContext webContext){
        for (int i = onOpenWebViewActivityListeners.size() - 1; i >= 0; i--) {
            onOpenWebViewActivityListeners.get(i).onOpenWebActivity(webContext);
        }
    }
}

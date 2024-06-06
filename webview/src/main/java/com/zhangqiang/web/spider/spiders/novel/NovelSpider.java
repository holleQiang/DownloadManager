package com.zhangqiang.web.spider.spiders.novel;

import android.graphics.Bitmap;
import android.webkit.WebView;

import com.zhangqiang.common.utils.JSONUtils;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.PageLoadListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.hybrid.methods.getElementsByTagName.GetElementsByTagName;
import com.zhangqiang.web.hybrid.methods.element.Element;
import com.zhangqiang.web.hybrid.plugins.JSCallPlugin;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.spider.Spider;

import java.util.List;

public class NovelSpider extends Spider {

    public NovelSpider(WebContext webContext) {
        super(webContext, "novel");
    }

    @Override
    protected void onStart() {
        WebContext webContext = getWebContext();
        State state = webContext.getState();
        if (state == State.WEB_VIEW_CREATE) {
            performStart();
        } else if (state != State.WEB_VIEW_DESTROY) {

            webContext.addOnStateChangeListener(onStateChangeListener);
        }
    }

    private void performStart() {
        WebContext webContext = getWebContext();
        if (webContext instanceof WebActivityContext) {
            WebActivityContext webActivityContext = (WebActivityContext) webContext;
            if (webActivityContext.isPageLoading()) {
                webActivityContext.removePageLoadListener(pageLoadListener);
                webActivityContext.addPageLoadListener(pageLoadListener);
            } else {
                performGetElements();
            }
        }
    }

    private void performGetElements() {
        getJSCallPlugin().invokeHybridMethod(new GetElementsByTagName("img", new GetElementsByTagName.ResultCallback() {
            @Override
            public void onSuccess(List<Element> elements) {
                WebLogger.info("=============" + JSONUtils.toJSONString(elements));
            }

            @Override
            public void onFail(Throwable e) {
                WebLogger.info("=============" + e.getMessage());
            }
        }));
    }

    private final PageLoadListener pageLoadListener = new PageLoadListener() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            ((WebActivityContext) getWebContext()).removePageLoadListener(this);
            performGetElements();
        }
    };

    private final OnStateChangeListener onStateChangeListener = new OnStateChangeListener() {
        @Override
        public void onStateChange(State state, State oldState) {
            if (state == State.WEB_VIEW_CREATE) {
                performStart();
            } else if (state == State.WEB_VIEW_DESTROY) {
                getWebContext().removeOnStateChangeListener(this);
            }
        }
    };

    @Override
    protected void onCancel() {

    }

    private JSCallPlugin getJSCallPlugin() {
        return (JSCallPlugin) WebManager.getInstance().findPluginOrThrow(new WebManager.Filter() {
            @Override
            public boolean onFilter(WebPlugin plugin) {
                return plugin instanceof JSCallPlugin;
            }
        });
    }
}

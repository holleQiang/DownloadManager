package com.zhangqiang.web.context;

import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.WebView;

import com.zhangqiang.common.utils.IntentUtils;
import com.zhangqiang.web.context.interceptors.Chain;
import com.zhangqiang.web.context.interceptors.RealCallChain;
import com.zhangqiang.web.context.interceptors.UrlLoadingInterceptor;

import java.util.ArrayList;
import java.util.List;

public class WebContext {

    private final List<OnStateChangeListener> onStateChangeListeners = new ArrayList<>();
    private final List<PageLoadListener> pageLoadListeners = new ArrayList<>();
    private final List<OnLoadResourceListener> onLoadResourceListeners = new ArrayList<>();
    private final List<OnReceiveTitleListener> onReceiveTitleListeners = new ArrayList<>();
    private final List<OnReceiveIconListener> onReceiveIconListeners = new ArrayList<>();
    private final List<UrlLoadingInterceptor> urlLoadingInterceptors = new ArrayList<>();

    private State mState = State.INITIAL;
    private WebView webView;
    private final String sessionId;
    private final String url;

    public WebContext(String sessionId, String url) {
        this.sessionId = sessionId;
        this.url = url;
    }

    private void dispatchState(State state) {
        State old = mState;
        if (state != old) {
            this.mState = state;
            dispatchStateChange(state, old);
        }
    }

    public State getState() {
        return mState;
    }

    public void dispatchOpenStart() {
        dispatchState(State.OPENING);
    }

    public void dispatchOpenFail(Throwable e) {
        dispatchState(State.OPEN_FAIL);
    }

    public void dispatchOpenSuccess() {
        dispatchState(State.OPEN_SUCCESS);
    }

    public void dispatchWebViewCreate(WebView webView) {
        this.webView = webView;
        dispatchState(State.WEB_VIEW_CREATE);
    }

    public void dispatchWebViewDestroy() {
        this.webView = null;
        dispatchState(State.WEB_VIEW_DESTROY);
    }

    public String getUrl() {
        return url;
    }

    public void addOnStateChangeListener(OnStateChangeListener listener) {
        if (onStateChangeListeners.contains(listener)) {
            return;
        }
        onStateChangeListeners.add(listener);
    }

    public void removeOnStateChangeListener(OnStateChangeListener listener) {
        onStateChangeListeners.remove(listener);
    }

    private void dispatchStateChange(State state, State oldState) {
        for (int i = onStateChangeListeners.size() - 1; i >= 0; i--) {
            onStateChangeListeners.get(i).onStateChange(state, oldState);
        }
    }

    public WebView getWebView() {
        return webView;
    }

    public void dispatchPageStarted(WebView view, String url, Bitmap favicon) {
        for (int i = pageLoadListeners.size() - 1; i >= 0; i--) {
            pageLoadListeners.get(i).onPageStarted(view, url, favicon);
        }
    }

    public void dispatchPageFinished(WebView view, String url) {
        for (int i = pageLoadListeners.size() - 1; i >= 0; i--) {
            pageLoadListeners.get(i).onPageFinished(view, url);
        }
    }

    public void addPageLoadListener(PageLoadListener listener) {
        if (pageLoadListeners.contains(listener)) {
            return;
        }
        pageLoadListeners.add(listener);
    }

    public void removePageLoadListener(PageLoadListener listener) {
        pageLoadListeners.remove(listener);
    }

    public String getSessionId() {
        return sessionId;
    }

    public void dispatchLoadResource(WebView view, String url) {
        for (int i = onLoadResourceListeners.size() - 1; i >= 0; i--) {
            onLoadResourceListeners.get(i).onLoadResource(view, url);
        }
    }

    public void addOnLoadResourceListener(OnLoadResourceListener listener) {
        if (onLoadResourceListeners.contains(listener)) {
            return;
        }
        onLoadResourceListeners.add(listener);
    }

    public void removeOnLoadResourceListener(OnLoadResourceListener listener) {
        onLoadResourceListeners.remove(listener);
    }


    public void dispatchReceiveTitle(String title) {
        for (int i = onReceiveTitleListeners.size() - 1; i >= 0; i--) {
            onReceiveTitleListeners.get(i).onReceiveTitle(title);
        }
    }

    public void addOnReceiveTitleListener(OnReceiveTitleListener listener) {
        if (onReceiveTitleListeners.contains(listener)) {
            return;
        }
        onReceiveTitleListeners.add(listener);
    }

    public void removeOnReceiveTitleListener(OnReceiveTitleListener listener) {
        onReceiveTitleListeners.remove(listener);
    }

    public void dispatchReceiveIcon(Bitmap icon) {
        for (int i = onReceiveIconListeners.size() - 1; i >= 0; i--) {
            onReceiveIconListeners.get(i).onReceiveIcon(icon);
        }
    }

    public void addOnReceiveIconListener(OnReceiveIconListener listener) {
        if (onReceiveIconListeners.contains(listener)) {
            return;
        }
        onReceiveIconListeners.add(listener);
    }

    public void removeOnReceiveIconListener(OnReceiveIconListener listener) {
        onReceiveIconListeners.remove(listener);
    }


    public boolean interceptUrlLoading(WebView view, String url) {
        List<UrlLoadingInterceptor> interceptors = new ArrayList<>(urlLoadingInterceptors);
        interceptors.add(new UrlLoadingInterceptor() {
            @Override
            public boolean onInterceptUrlLoading(Chain chain) {
                Uri uri = Uri.parse(url);
                String scheme = uri.getScheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    return false;
                } else {
                    try {
                        IntentUtils.openActivityByUri(view.getContext(), uri);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
        });
        return new RealCallChain(url, interceptors, 0).proceed(url);
    }

    public void addUrlLoadingInterceptor(UrlLoadingInterceptor interceptor) {
        if (urlLoadingInterceptors.contains(interceptor)) {
            return;
        }
        urlLoadingInterceptors.add(interceptor);
    }

    public void removeUrlLoadingInterceptor(UrlLoadingInterceptor interceptor) {
        urlLoadingInterceptors.remove(interceptor);
    }
}

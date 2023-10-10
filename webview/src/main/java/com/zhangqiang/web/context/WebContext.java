package com.zhangqiang.web.context;

import android.graphics.Bitmap;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

public class WebContext {

    private final List<OnStateChangeListener> onStateChangeListeners = new ArrayList<>();
    private final List<PageLoadListener> pageLoadListeners = new ArrayList<>();

    private State mState = State.INITIAL;
    private WebView webView;
    private final String url;

    public WebContext(String url) {
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

    public void addPageLoadListener(PageLoadListener listener){
        pageLoadListeners.add(listener);
    }

    public void removePageLoadListener(PageLoadListener listener){
        pageLoadListeners.remove(listener);
    }
}

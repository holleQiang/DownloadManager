package com.zhangqiang.web;

import android.os.Looper;
import android.webkit.WebView;

import androidx.fragment.app.FragmentManager;

public class WebContext {

    public static final int STATE_OPENING = 0;
    public static final int STATE_WEB_VIEW_CREATE = 0;
    public static final int STATE_WEB_VIEW_DESTROY = 0;

    private int mState = STATE_OPENING;
    public FragmentManager fragmentManager;
    public Looper looper;
    public WebView webView;

    public void dispatchState(int state){
        if(mState != state){
            this.mState = state;
        }
    }

    public int getState() {
        return mState;
    }

    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }
}

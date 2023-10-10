package com.zhangqiang.web.activity;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.context.WebContext;

import java.util.ArrayList;
import java.util.List;

public class WebActivityContext extends WebContext {

    public FragmentActivity activity;
    private final List<OnActivityCreatedListener> onActivityCreatedListeners = new ArrayList<>();

    public WebActivityContext(String url) {
        super(url);
    }

    public void dispatchActivityCreate(FragmentActivity activity) {
        this.activity = activity;
        for (int i = onActivityCreatedListeners.size() - 1; i >= 0; i--) {
            onActivityCreatedListeners.get(i).onActivityCreated(activity);
        }
    }

    public void dispatchActivityDestroy() {
        this.activity = null;
    }

    public FragmentActivity getActivity() {
        return activity;
    }

    public void addOnActivityCreatedListener(OnActivityCreatedListener listener){
        onActivityCreatedListeners.add(listener);
    }

    public void removeOnActivityCreatedListener(OnActivityCreatedListener listener){
        onActivityCreatedListeners.remove(listener);
    }
}

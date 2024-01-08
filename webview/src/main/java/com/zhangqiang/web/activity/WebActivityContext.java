package com.zhangqiang.web.activity;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.context.WebContext;

import java.util.ArrayList;
import java.util.List;

public class WebActivityContext extends WebContext {

    public FragmentActivity activity;
    private final List<OnActivityCreatedListener> onActivityCreatedListeners = new ArrayList<>();
    private final List<OnActivityDestroyListener> onActivityDestroyListeners = new ArrayList<>();

    public WebActivityContext(String id,String url) {
        super(id, url);
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

    public void addOnActivityCreatedListener(OnActivityCreatedListener listener){
        if(onActivityCreatedListeners.contains(listener)){
            return;
        }
        onActivityCreatedListeners.add(listener);
    }

    public void removeOnActivityCreatedListener(OnActivityCreatedListener listener){
        onActivityCreatedListeners.remove(listener);
    }

    public void addOnActivityDestroyListener(OnActivityDestroyListener listener){
        if(onActivityDestroyListeners.contains(listener)){
            return;
        }
        onActivityDestroyListeners.add(listener);
    }

    public void removeOnActivityDestroyListener(OnActivityDestroyListener listener){
        onActivityDestroyListeners.remove(listener);
    }
}

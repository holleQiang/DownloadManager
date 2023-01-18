package com.zhangqiang.downloadmanager.task.http.part;

import java.util.ArrayList;
import java.util.List;

public class Callbacks {

    private List<Callback> callbacks;

    public synchronized void addCallback(Callback callback){
        if(callbacks == null){
            callbacks = new ArrayList<>();
        }
        callbacks.add(callback);
    }

    public synchronized void removeCallback(Callback callback){
        if(callbacks == null){
            return;
        }
        callbacks.remove(callback);
    }

    public synchronized void notifyStateChange(){
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onStateChange();
        }
    }
}

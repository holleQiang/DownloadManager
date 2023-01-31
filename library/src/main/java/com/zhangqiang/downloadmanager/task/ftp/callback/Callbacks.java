package com.zhangqiang.downloadmanager.task.ftp.callback;

import java.util.ArrayList;
import java.util.List;

public class Callbacks {
    private List<Callback> callbackList;

    public void addCallback(Callback callback){
        if (callbackList == null) {
            callbackList  = new ArrayList<>();
        }
        if (!callbackList.contains(callback)) {
            callbackList.add(callback);
        }
    }

    public void removeCallback(Callback callback){
        if (callbackList == null) {
            return;
        }
        callbackList.remove(callback);
    }

    public synchronized void notifyResourceInfoReady(ResourceInfo resourceInfo){
        if (callbackList == null) {
            return;
        }
        for (int i = callbackList.size() - 1; i >= 0; i--) {
            callbackList.get(i).onResourceInfoReady(resourceInfo);
        }
    }
}

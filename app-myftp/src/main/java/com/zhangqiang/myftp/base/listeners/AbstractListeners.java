package com.zhangqiang.myftp.base.listeners;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractListeners<T>{

    protected List<T> listenerList;

    public synchronized void addListener(T listener){
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
        if(listenerList.contains(listener)){
            return;
        }
        listenerList.add(listener);
    }

    public synchronized void removeListener(T listener){
        if (listenerList == null) {
            return;
        }
        listenerList.remove(listener);
    }
}

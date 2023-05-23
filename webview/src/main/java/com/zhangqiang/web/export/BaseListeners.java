package com.zhangqiang.web.export;

import java.util.ArrayList;
import java.util.List;

public class BaseListeners<T> {

    private List<T> listeners = null;

    public synchronized void add(T t) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (listeners.contains(t)) {
            return;
        }
        listeners.add(t);
    }

    public synchronized void remove(T t) {
        if (listeners == null) {
            return;
        }
        listeners.remove(t);
    }

    protected synchronized void doTraversal(TraversalFunc<T> func){
        if(listeners == null){
            return;
        }
        for (int i = listeners.size() - 1; i >= 0; i--) {
            func.run(listeners.get(i));
        }
    }

    protected interface TraversalFunc<T>{
       void run(T t);
    }
}

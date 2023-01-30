package com.zhangqiang.downloadmanager.task.http.callback;

import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;

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

    public synchronized void notifyStartGenerateInfo(){
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onStartGenerateInfo();
        }
    }

    public synchronized void notifyResourceInfoReady(ResourceInfo resourceInfo){
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onResourceInfoReady(resourceInfo);
        }
    }

    public synchronized void notifyStartDefaultDownload(){
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onStartDefaultDownload();
        }
    }

    public synchronized void notifyStartPartDownload(){
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onStartPartDownload();
        }
    }

    public synchronized void notifyPartTaskFail(HttpDownloadPartTask task,Throwable e) {
        if (callbacks == null) {
            return;
        }
        for (int i = callbacks.size() - 1; i >= 0; i--) {
            callbacks.get(i).onPartTaskFail(task,e);
        }
    }
}

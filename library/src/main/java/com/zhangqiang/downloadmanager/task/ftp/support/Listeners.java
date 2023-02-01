package com.zhangqiang.downloadmanager.task.ftp.support;

import java.util.ArrayList;
import java.util.List;

public class Listeners {

    private List<FTPTaskInfo.Listener> listenerList;

    synchronized void addListener(FTPTaskInfo.Listener listener) {
        if (listenerList == null) {
            listenerList = new ArrayList<>();
        }
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
    }

    synchronized  void removeListener(FTPTaskInfo.Listener listener) {
        if (listenerList == null) {
            return;
        }
        listenerList.remove(listener);
    }

    synchronized  void notifyInfoReady(){
        if (listenerList == null) {
            return;
        }
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).onInfoReady();
        }
    }
    synchronized  void notifyStateChanged(){
        if (listenerList == null) {
            return;
        }
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).onStateChanged();
        }
    }
    synchronized  void notifySeedChanged(){
        if (listenerList == null) {
            return;
        }
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).onSpeedChanged();
        }
    }
    synchronized  void notifyProgressChanged(){
        if (listenerList == null) {
            return;
        }
        for (int i = listenerList.size() - 1; i >= 0; i--) {
            listenerList.get(i).onProgressChanged();
        }
    }
}

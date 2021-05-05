package com.zhangqiang.downloadmanager.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;
import java.util.List;

public class NetWorkManager {

    private static volatile NetWorkManager instance;
    private final List<OnAvailableChangedListener> onAvailableChangedListeners = new ArrayList<>();
    private final Context mContext;

    private NetWorkManager(Context context) {
        mContext = context.getApplicationContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean available = isAvailable();
                for (int i = onAvailableChangedListeners.size() - 1; i >= 0; i--) {
                    onAvailableChangedListeners.get(i).onAvailableChanged(available);
                }
            }
        }, filter);
    }

    public static NetWorkManager getInstance(Context context){
        if (instance == null) {
            synchronized (NetWorkManager.class){
                if (instance == null) {
                    instance = new NetWorkManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public interface OnAvailableChangedListener{
        void onAvailableChanged(boolean available);
    }

    public void addOnAvailableChangedListener(OnAvailableChangedListener listener){
        if (onAvailableChangedListeners.contains(listener)) {
            return;
        }
        onAvailableChangedListeners.add(listener);
    }

    public void removeOnAvailableChangedListener(OnAvailableChangedListener listener){
        onAvailableChangedListeners.remove(listener);
    }

    public boolean isAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo!= null && activeNetworkInfo.isAvailable();
        }
        return false;
    }
}

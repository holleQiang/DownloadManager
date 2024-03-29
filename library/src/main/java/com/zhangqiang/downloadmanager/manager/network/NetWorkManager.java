package com.zhangqiang.downloadmanager.manager.network;

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
    private ConnectivityManager mConnectivityManager;
    private boolean isAvailable;

    private NetWorkManager(Context context) {
        mContext = context.getApplicationContext();
        isAvailable = isNetWorkAvailable();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean available = isNetWorkAvailable();
                if (available != isAvailable) {
                    isAvailable = available;
                    notifyAvailableChanged(available);
                }
            }
        }, filter);
    }

    public static NetWorkManager getInstance(Context context) {
        if (instance == null) {
            synchronized (NetWorkManager.class) {
                if (instance == null) {
                    instance = new NetWorkManager(context);
                }
            }
        }
        return instance;
    }

    public synchronized void addOnAvailableChangedListener(OnAvailableChangedListener listener) {
        if (onAvailableChangedListeners.contains(listener)) {
            return;
        }
        onAvailableChangedListeners.add(listener);
    }

    public synchronized void removeOnAvailableChangedListener(OnAvailableChangedListener listener) {
        onAvailableChangedListeners.remove(listener);
    }

    private synchronized void notifyAvailableChanged(boolean available) {
        for (int i = onAvailableChangedListeners.size() - 1; i >= 0; i--) {
            onAvailableChangedListeners.get(i).onAvailableChanged(available);
        }
    }

    private boolean isNetWorkAvailable() {
        ConnectivityManager connectivityManager = getConnectivityManager();
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
        }
        return false;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    private synchronized ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        return mConnectivityManager;
    }
}

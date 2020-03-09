package com.zhangqiang.downloadmanager.utils;

import android.util.Log;

public class LogUtils {

    public static final boolean DEBUG = true;

    public static void i(String TAG, String log) {
        if (!DEBUG) {
            return;
        }
        Log.i(TAG, log);
    }
}

package com.zhangqiang.web.log;

import android.util.Log;

public class WebLogger {

    private static final String TAG = "WebLog";

    public static void info(String message) {
        Log.i(TAG, "info:\n-----------------------------\n"
                + message
                + "\n-----------------------------\n");
    }

    public static void error(String message) {
        Log.e(TAG, "error:\n-----------------------------\n"
                + message
                + "\n-----------------------------\n");
    }
}

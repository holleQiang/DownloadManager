package com.zhangqiang.common.utils;

import android.os.Handler;
import android.os.Looper;

public class ThreadUtils {

    public static void doOnUIThread(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }
}

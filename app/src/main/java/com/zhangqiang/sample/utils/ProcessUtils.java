package com.zhangqiang.sample.utils;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Process;
import android.text.TextUtils;

import androidx.annotation.RequiresApi;

import com.zhangqiang.sample.BuildConfig;

import java.lang.reflect.Method;
import java.util.List;

public class ProcessUtils {

    public static String getCurrentProcessName(Context context) {
        String currentProcessName = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            currentProcessName = getCurrentProcessNameByApplication();
        }
        if (TextUtils.isEmpty(currentProcessName)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                currentProcessName = getCurrentProcessNameByActivityThread();
            }
        }
        if (TextUtils.isEmpty(currentProcessName)) {
            currentProcessName = getCurrentProcessNameByActivityManager(context);
        }
        return currentProcessName;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static String getCurrentProcessNameByApplication() {
        return Application.getProcessName();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("DiscouragedPrivateApi")
    public static String getCurrentProcessNameByActivityThread() {

        try {
            @SuppressLint("PrivateApi") Method declaredMethod = Class.forName("android.app.ActivityThread",
                            false,
                            Application.class.getClassLoader())
                    .getDeclaredMethod("currentProcessName");
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null);
            if (invoke instanceof String) {
                return (String) invoke;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getCurrentProcessNameByActivityManager(Context context) {

        int pid = Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if (runningAppProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.pid == pid) {
                    return runningAppProcess.processName;
                }

            }
        }
        return null;
    }

    public static boolean isMainProcess(Context context) {
        return BuildConfig.APPLICATION_ID.equals(getCurrentProcessName(context));
    }
}

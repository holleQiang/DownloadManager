package com.zhangqiang.keystore;

import android.content.Context;
import android.content.SharedPreferences;

public class Options {

    private static Context mContext;
    private static SharedPreferences sharedPreferences;

    public static void init(Context context) {
        mContext = context.getApplicationContext();
        sharedPreferences = context.getSharedPreferences("options", 0);
    }

    public static Option<Integer> ofInt(String key, Integer defaultValue) {
        return new SharedIntOption(key, defaultValue, sharedPreferences);
    }

    public static Option<String> ofString(String key, String defaultValue) {
        return new SharedStringOption(key, defaultValue, sharedPreferences);
    }
}

package com.zhangqiang.keystore;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class SharedIntOption extends SharedOption<Integer> {


    public SharedIntOption(@NonNull String key, Integer defaultValue, SharedPreferences sharedPreferences) {
        super(key, defaultValue, sharedPreferences);
    }

    @Override
    protected void saveValue(String key, Integer value) {
        getSharedPreferences().edit().putInt(key, value).apply();
    }

    @Override
    protected Integer getValue(String key, Integer defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

}

package com.zhangqiang.keystore;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public abstract class SharedOption<V> extends Option<V> {

    private SharedPreferences sharedPreferences;

    public SharedOption(@NonNull String key, V defaultValue, SharedPreferences sharedPreferences) {
        super(key, defaultValue);
        this.sharedPreferences = sharedPreferences;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}

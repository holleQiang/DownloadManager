package com.zhangqiang.keystore.store.shared;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhangqiang.keystore.store.ValueStore;

public class SharedValueStore implements ValueStore {

    private SharedPreferences sharedPreferences;
    private final Context context;
    private final String fileName;

    public SharedValueStore(Context context, String fileName) {
        this.context = context.getApplicationContext();
        this.fileName = fileName;
    }

    @Override
    public void put(String key, int value) {
        getSharedPreferences().edit().putInt(key, value).apply();
    }

    @Override
    public void put(String key, String value) {
        getSharedPreferences().edit().putString(key, value).apply();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return getSharedPreferences().getInt(key, defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return getSharedPreferences().getString(key, defaultValue);
    }

    public SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            synchronized (this) {
                if (sharedPreferences == null) {
                    sharedPreferences = context.getSharedPreferences(fileName, 0);
                }
            }
        }
        return sharedPreferences;
    }
}

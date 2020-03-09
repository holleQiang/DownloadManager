package com.zhangqiang.keystore;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class SharedStringOption extends Option<String> {

    private SharedPreferences sharedPreferences;

    public SharedStringOption(@NonNull String key, String defaultValue, SharedPreferences sharedPreferences) {
        super(key, defaultValue);
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    protected void saveValue(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Override
    protected String getValue(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}

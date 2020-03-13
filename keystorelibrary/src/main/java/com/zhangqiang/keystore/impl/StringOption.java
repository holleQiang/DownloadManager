package com.zhangqiang.keystore.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.keystore.store.ValueStore;

public class StringOption extends BaseOption<String> {

    public StringOption(@NonNull String key, @Nullable String defaultValue, ValueStore valueStore) {
        super(key, defaultValue, valueStore);
    }

    @Override
    protected void saveValue(String key, String value) {
        getValueStore().put(key, value);
    }

    @Override
    protected String getValue(String key, String defaultValue) {
        return getValueStore().getString(key, defaultValue);
    }
}

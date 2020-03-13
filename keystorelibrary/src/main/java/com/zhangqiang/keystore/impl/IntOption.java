package com.zhangqiang.keystore.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.keystore.store.ValueStore;

public class IntOption extends BaseOption<Integer> {


    public IntOption(@NonNull String key, @Nullable Integer defaultValue, ValueStore valueStore) {
        super(key, defaultValue, valueStore);
    }

    @Override
    protected void saveValue(String key, Integer value) {
        getValueStore().put(key,value);
    }

    @Override
    protected Integer getValue(String key, Integer defaultValue) {
        return getValueStore().getInt(key,defaultValue);
    }
}

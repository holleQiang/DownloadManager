package com.zhangqiang.keystore.impl;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.keystore.Option;
import com.zhangqiang.keystore.store.ValueStore;

public abstract class BaseOption<V> extends Option<V> {

    private final ValueStore valueStore;

    public BaseOption(@NonNull String key, @Nullable V defaultValue, ValueStore valueStore) {
        super(key, defaultValue);
        this.valueStore = valueStore;
    }

    public ValueStore getValueStore() {
        return valueStore;
    }
}

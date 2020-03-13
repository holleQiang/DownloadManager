package com.zhangqiang.keystore.store.mmkv;

import android.content.Context;

import com.zhangqiang.keystore.store.ValueStore;

public class MMKVValueStore implements ValueStore {

    private Context context;

    public MMKVValueStore(Context context) {
        this.context = context;
    }

    @Override
    public void put(String key, int value) {
        MMKVUtils.getInstance(context).putInt(key,value);
    }

    @Override
    public void put(String key, String value) {
        MMKVUtils.getInstance(context).putString(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return MMKVUtils.getInstance(context).getInt(key, defaultValue);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return MMKVUtils.getInstance(context).getString(key, defaultValue);
    }
}

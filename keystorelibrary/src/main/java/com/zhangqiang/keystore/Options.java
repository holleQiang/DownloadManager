package com.zhangqiang.keystore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.zhangqiang.keystore.impl.IntOption;
import com.zhangqiang.keystore.impl.StringOption;
import com.zhangqiang.keystore.store.ValueStore;

public class Options {


    public static Option<Integer> ofInt(@NonNull String key, @Nullable Integer defaultValue, ValueStore valueStore) {
        return new IntOption(key, defaultValue, valueStore);
    }

    public static Option<String> ofString(@NonNull String key, @Nullable String defaultValue, ValueStore valueStore) {
        return new StringOption(key, defaultValue, valueStore);
    }
}

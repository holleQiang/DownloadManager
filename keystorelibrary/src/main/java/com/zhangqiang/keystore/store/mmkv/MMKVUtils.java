package com.zhangqiang.keystore.store.mmkv;

import android.content.Context;

import com.tencent.mmkv.MMKV;

public class MMKVUtils {

    private static volatile MMKV mmkv;

    public static MMKV getInstance(Context context) {
        if (mmkv == null) {
            synchronized (MMKVUtils.class) {
                if (mmkv == null) {
                    MMKV.initialize(context);
                    mmkv = MMKV.defaultMMKV();
                }
            }
        }
        return mmkv;
    }
}

package com.zhangqiang.keystore.store;

public interface ValueStore {

    void put(String key, int value);

    void put(String key, String value);

    int getInt(String key, int defaultValue);

    String getString(String key, String defaultValue);


}

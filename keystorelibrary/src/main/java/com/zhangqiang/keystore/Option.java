package com.zhangqiang.keystore;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class Option<V> {

    private final String key;
    private final V defaultValue;
    private V value;
    private List<OnValueChangedListener> onValueChangedListeners;

    public Option(@NonNull String key, @Nullable V defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    public void set(V value) {
        if (this.value != value) {
            this.value = value;
            saveValue(key, value);
            if (onValueChangedListeners != null) {
                for (int i = onValueChangedListeners.size() - 1; i >= 0; i--) {
                    onValueChangedListeners.get(i).onValueChanged();
                }
            }
        }
    }

    protected abstract void saveValue(String key, V value);

    protected abstract V getValue(String key, V defaultValue);

    public V get() {
        if (value != null) {
            return value;
        }
        return getValue(key, defaultValue);
    }

    public void addOnValueChangedListener(OnValueChangedListener listener) {
        if (onValueChangedListeners == null) {
            onValueChangedListeners = new ArrayList<>();
        }
        if (onValueChangedListeners.contains(listener)) {
            return;
        }
        onValueChangedListeners.add(listener);
    }

    public void removeOnValueChangedListener(OnValueChangedListener listener) {
        if (onValueChangedListeners == null) {
            return;
        }
        onValueChangedListeners.remove(listener);
    }
}

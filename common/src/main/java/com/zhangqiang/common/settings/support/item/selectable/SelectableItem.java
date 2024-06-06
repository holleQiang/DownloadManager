package com.zhangqiang.common.settings.support.item.selectable;

import androidx.annotation.NonNull;

public class SelectableItem {

    private final String title;

    public SelectableItem(String title) {
        this.title = title;
    }

    @NonNull
    @Override
    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }
}

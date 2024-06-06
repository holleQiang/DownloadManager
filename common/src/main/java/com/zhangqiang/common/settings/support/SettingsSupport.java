package com.zhangqiang.common.settings.support;

import com.zhangqiang.common.settings.support.item.SettingsItem;

import java.util.List;

public class SettingsSupport {

    private final List<SettingsItem> itemSupports;

    public SettingsSupport(List<SettingsItem> itemSupports) {
        this.itemSupports = itemSupports;
    }

    public List<SettingsItem> getItemSupports() {
        return itemSupports;
    }
}

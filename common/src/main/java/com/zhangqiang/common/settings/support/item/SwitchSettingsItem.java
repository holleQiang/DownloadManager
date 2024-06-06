package com.zhangqiang.common.settings.support.item;

public abstract class SwitchSettingsItem extends SettingsItem {

    public abstract String getTitle();

    public abstract void onSwitchStatusChange(boolean checked);

    public abstract boolean getSwitchStatus();

}

package com.zhangqiang.common.settings.support.item.selectable;

import com.zhangqiang.common.settings.support.item.SettingsItem;

import java.util.List;

public abstract class SelectableSettingsItem extends SettingsItem {

    public abstract int getSelectedPosition();
    public abstract List<? extends SelectableItem> getSelectableItems();
    public abstract String getTitle();

    public abstract void onSelectedItemChange(int position,SelectableItem selectableItem);
}

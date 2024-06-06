package com.zhangqiang.common.settings.cell;

import android.widget.CompoundButton;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.common.R;
import com.zhangqiang.common.settings.support.item.SwitchSettingsItem;

public class SwitchCell extends MultiCell<SwitchSettingsItem> {
    public SwitchCell(SwitchSettingsItem data) {
        super(R.layout.settings_item_switch, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        SwitchSettingsItem data = getData();
        vh.setText(R.id.tv_title, data.getTitle());
        vh.setChecked(R.id.cb_switch, data.getSwitchStatus());
        vh.setOnCheckedChangeListener(R.id.cb_switch, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                data.onSwitchStatusChange(isChecked);
            }
        });
    }
}

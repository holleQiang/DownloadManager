package com.zhangqiang.common.settings.cell.selectable;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.common.R;
import com.zhangqiang.common.settings.support.item.selectable.SelectableItem;
import com.zhangqiang.common.settings.support.item.selectable.SelectableSettingsItem;

import java.util.List;

public class SelectableSettingsCell extends MultiCell<SelectableSettingsItem> {
    public SelectableSettingsCell(SelectableSettingsItem data) {
        super(R.layout.settings_item_selectable, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        SelectableSettingsItem data = getData();
        vh.setText(R.id.tv_title, data.getTitle());
        Spinner spinner = vh.getView(R.id.spinner);
        List<? extends SelectableItem> selectableItems = data.getSelectableItems();
        ArrayAdapter<? extends SelectableItem> adapter = new ArrayAdapter<>(vh.getView().getContext(),
                R.layout.support_simple_spinner_dropdown_item,selectableItems);
        spinner.setAdapter(adapter);
        
        int selectedPosition = data.getSelectedPosition();
        if(selectedPosition >= 0 && selectedPosition < selectableItems.size()){
            spinner.setSelection(selectedPosition);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                data.onSelectedItemChange(position,adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                data.onSelectedItemChange(-1,null);
            }
        });
    }
}

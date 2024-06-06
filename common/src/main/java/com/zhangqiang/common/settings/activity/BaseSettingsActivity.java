package com.zhangqiang.common.settings.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.common.R;
import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.common.settings.cell.SwitchCell;
import com.zhangqiang.common.settings.cell.selectable.SelectableSettingsCell;
import com.zhangqiang.common.settings.support.item.SettingsItem;
import com.zhangqiang.common.settings.support.SettingsSupport;
import com.zhangqiang.common.settings.support.item.SwitchSettingsItem;
import com.zhangqiang.common.settings.support.item.selectable.SelectableSettingsItem;

import java.util.List;

public abstract class BaseSettingsActivity extends BaseActivity {

    private SettingsSupport mSettingsSupport;

    public SettingsSupport getSettingsSupport() {
        if (mSettingsSupport == null) {
            mSettingsSupport = onCreateSettingsSupport();
        }
        return mSettingsSupport;
    }

    public abstract SettingsSupport onCreateSettingsSupport();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_common);
        Toolbar mToolbar = findViewById(R.id.m_settings_toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setSupportActionBar(mToolbar);
        RecyclerView rvSettings = findViewById(R.id.rv_settings);
        rvSettings.setLayoutManager(new LinearLayoutManager(this));
        CellRVAdapter adapter = new CellRVAdapter();
        rvSettings.setAdapter(adapter);

        SettingsSupport settingsSupport = getSettingsSupport();
        List<SettingsItem> itemSupports = settingsSupport.getItemSupports();
        if (itemSupports != null) {
            for (SettingsItem itemSupport : itemSupports) {
                if (itemSupport instanceof SwitchSettingsItem) {
                    adapter.addDataAtLast(new SwitchCell((SwitchSettingsItem) itemSupport));
                } else if (itemSupport instanceof SelectableSettingsItem) {
                    adapter.addDataAtLast(new SelectableSettingsCell((SelectableSettingsItem) itemSupport));
                }
            }
        }
    }
}

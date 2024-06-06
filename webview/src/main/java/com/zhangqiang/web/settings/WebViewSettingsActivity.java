package com.zhangqiang.web.settings;

import com.zhangqiang.common.settings.activity.BaseSettingsActivity;
import com.zhangqiang.common.settings.support.SettingsSupport;
import com.zhangqiang.common.settings.support.item.SettingsItem;
import com.zhangqiang.web.settings.support.UserAgentSettingsItem;

import java.util.ArrayList;

public class WebViewSettingsActivity extends BaseSettingsActivity {

    @Override
    public SettingsSupport onCreateSettingsSupport() {
        ArrayList<SettingsItem> itemSupports = new ArrayList<>();
        itemSupports.add(new UserAgentSettingsItem(this));
        return new SettingsSupport(itemSupports);
    }
}

package com.zhangqiang.web.settings.support;

import android.content.Context;

import com.zhangqiang.common.settings.support.item.selectable.SelectableItem;
import com.zhangqiang.common.settings.support.item.selectable.SelectableSettingsItem;
import com.zhangqiang.web.settings.WebViewSettings;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

public class UserAgentSettingsItem extends SelectableSettingsItem {

    private final Context context;
    private final List<UserAgentSelectableItem> selectableItems = new ArrayList<>();

    public UserAgentSettingsItem(Context context) {
        this.context = context;
        selectableItems.add(new UserAgentSelectableItem(context.getString(R.string.mobile), WebViewSettings.USER_AGENT_TYPE_MOBILE));
        selectableItems.add(new UserAgentSelectableItem(context.getString(R.string.pc), WebViewSettings.USER_AGENT_TYPE_PC));
    }

    @Override
    public int getSelectedPosition() {
        int userAgentType = WebViewSettings.get(context).getUserAgentType();
        int position = -1;
        for (int i = 0; i < selectableItems.size(); i++) {
            if (selectableItems.get(i).getUserAgentType() == userAgentType) {
                position = i;
            }
        }
        return position;
    }

    @Override
    public List<? extends SelectableItem> getSelectableItems() {
        return selectableItems;
    }

    @Override
    public String getTitle() {
        return context.getString(R.string.user_agent);
    }

    @Override
    public void onSelectedItemChange(int position, SelectableItem selectableItem) {
        UserAgentSelectableItem userAgentSelectableItem = (UserAgentSelectableItem) selectableItem;
        int userAgentType = userAgentSelectableItem.getUserAgentType();
        if (userAgentType == WebViewSettings.USER_AGENT_TYPE_MOBILE) {
            WebViewSettings.get(context).setUserAgentType(WebViewSettings.USER_AGENT_TYPE_MOBILE);
        } else if (userAgentType == WebViewSettings.USER_AGENT_TYPE_PC) {
            WebViewSettings.get(context).setUserAgentType(WebViewSettings.USER_AGENT_TYPE_PC);
        }
    }

}

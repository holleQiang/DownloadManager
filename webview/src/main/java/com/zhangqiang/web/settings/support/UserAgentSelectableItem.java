package com.zhangqiang.web.settings.support;

import com.zhangqiang.common.settings.support.item.selectable.SelectableItem;

public class UserAgentSelectableItem extends SelectableItem {

    private final int userAgentType;

    public UserAgentSelectableItem(String title, int userAgentType) {
        super(title);
        this.userAgentType = userAgentType;
    }

    public int getUserAgentType() {
        return userAgentType;
    }
}

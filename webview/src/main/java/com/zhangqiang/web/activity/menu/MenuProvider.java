package com.zhangqiang.web.activity.menu;

import java.util.List;

public interface MenuProvider {

    List<MenuItemBean> provideMenuItems();

    void onMenuItemClick(MenuItemBean menuItemBean);
}

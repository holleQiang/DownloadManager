package com.zhangqiang.web.activity.menu;

import java.util.List;

public class MenuItemBean {

    private int id;
    private String title;
    /**
     * Sync to attrs.xml enum, values in MenuItem: - 0: never - 1: ifRoom - 2: always - -1: Safe sentinel for "no
     */
    private int showAsAction;
    private List<MenuItemBean> subMenuItems;

    public int getId() {
        return id;
    }

    public MenuItemBean setId(int id) {
        this.id = id;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MenuItemBean setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<MenuItemBean> getSubMenuItems() {
        return subMenuItems;
    }

    public MenuItemBean setSubMenuItems(List<MenuItemBean> subMenuItems) {
        this.subMenuItems = subMenuItems;
        return this;
    }

    public int getShowAsAction() {
        return showAsAction;
    }

    public MenuItemBean setShowAsAction(int showAsAction) {
        this.showAsAction = showAsAction;
        return this;
    }
}

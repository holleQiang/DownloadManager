package com.zhangqiang.web.spider;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.spider.bean.SpiderBean;
import com.zhangqiang.web.spider.dialog.SpiderDialog;
import com.zhangqiang.web.spider.spiders.novel.NovelSpider;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SpiderPlugin implements WebPlugin {
    public static final int MENU_ID_SPIDER = 7;
    private final List<SpiderBean> spiderBeans = new ArrayList<>();

    public static final String SPIDER_TYPE_NOVEL = "0";

    @Override

    public void apply(PluginContext pluginContext) {
        SpiderBean spiderBean = new SpiderBean();
        spiderBean.setName("novel");
        spiderBean.setType(SPIDER_TYPE_NOVEL);
        spiderBeans.add(spiderBean);
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean().setId(MENU_ID_SPIDER).setTitle(webContext.getActivity().getString(R.string.spider)));
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        int id = menuItemBean.getId();
                        if (id == MENU_ID_SPIDER) {
                            SpiderDialog spiderDialog = SpiderDialog.newInstance();
                            spiderDialog.setOnSpiderClickListener(new SpiderDialog.OnSpiderClickListener() {
                                @Override
                                public void onSpiderClick(SpiderBean spiderBean, int position) {
                                    if (Objects.equals(spiderBean.getType(), SPIDER_TYPE_NOVEL)) {
                                        new NovelSpider(webContext).start();
                                    }
                                }
                            });
                            spiderDialog.show(webContext.getActivity().getSupportFragmentManager(), "spider_dialog");
                        }
                    }
                });
            }
        });
    }

    public List<SpiderBean> getSpiders() {
        return spiderBeans;
    }
}

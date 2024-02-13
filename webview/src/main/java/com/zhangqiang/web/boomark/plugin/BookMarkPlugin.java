package com.zhangqiang.web.boomark.plugin;

import android.text.TextUtils;

import androidx.fragment.app.FragmentActivity;

import com.zhangqiang.web.activity.OnActivityCreatedListener;
import com.zhangqiang.web.activity.OnActivityDestroyListener;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.activity.menu.MenuProvider;
import com.zhangqiang.web.boomark.bean.BookMarkBean;
import com.zhangqiang.web.boomark.dialog.BookmarkTitleEditDialog;
import com.zhangqiang.web.boomark.dialog.BookMarkDialog;
import com.zhangqiang.web.boomark.service.BookMarkService;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BookMarkPlugin implements WebPlugin {
    private static final int MENU_ID_BOOKMARK = 3;
    private static final int MENU_ID_BOOKMARK_HANDLER = 4;

    private BookMarkService bookMarkService;

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnActivityCreatedListener(new OnActivityCreatedListener() {
                    @Override
                    public void onActivityCreated(FragmentActivity activity) {
                        bookMarkService = new BookMarkService(activity);
                    }
                });
                webContext.addOnActivityDestroyListener(new OnActivityDestroyListener() {
                    @Override
                    public void onActivityDestroy() {
                        bookMarkService = null;
                    }
                });
                webContext.addMenuProvider(new MenuProvider() {
                    @Override
                    public List<MenuItemBean> provideMenuItems() {
                        ArrayList<MenuItemBean> menuItemBeans = new ArrayList<>();
                        menuItemBeans.add(new MenuItemBean()
                                .setId(MENU_ID_BOOKMARK)
                                .setTitle(webContext.getActivity().getString(R.string.bookmark)));
                        String url = webContext.getWebView().getUrl();
                        if (!TextUtils.isEmpty(url)) {
                            boolean exists = bookMarkService.isExists(url);
                            String title;
                            if (exists) {
                                title = webContext.getActivity().getString(R.string.remove_bookmark);
                            } else {
                                title = webContext.getActivity().getString(R.string.add_book_mark);
                            }
                            menuItemBeans.add(new MenuItemBean()
                                    .setId(MENU_ID_BOOKMARK_HANDLER)
                                    .setTitle(title));
                        }
                        return menuItemBeans;
                    }

                    @Override
                    public void onMenuItemClick(MenuItemBean menuItemBean) {
                        if (menuItemBean.getId() == MENU_ID_BOOKMARK) {
                            BookMarkDialog.newInstance()
                                    .setOnBookmarkClickListener(new BookMarkDialog.OnBookmarkClickListener() {
                                        @Override
                                        public void onBookmarkClick(BookMarkBean bookMarkBean) {
                                            webContext.getActivity().performSearch(bookMarkBean.getUrl());
                                        }
                                    })
                                    .show(webContext.getActivity().getSupportFragmentManager(), "bookmark");
                        } else if (menuItemBean.getId() == MENU_ID_BOOKMARK_HANDLER) {
                            final String url = webContext.getWebView().getUrl();
                            if (bookMarkService.isExists(url)) {
                                bookMarkService.deleteByUrl(url);
                                webContext.getActivity().invalidateOptionsMenu();
                            } else {
                                BookmarkTitleEditDialog.newInstance(webContext.getWebView().getTitle())
                                        .setOnConfirmListener(new BookmarkTitleEditDialog.OnConfirmListener() {
                                            @Override
                                            public void onConfirm(String title) {
                                                bookMarkService.add(new BookMarkBean()
                                                        .setId(UUID.randomUUID().toString())
                                                        .setTitle(title)
                                                        .setUrl(url), null);
                                                webContext.getActivity().invalidateOptionsMenu();
                                            }
                                        })
                                        .show(webContext.getActivity().getSupportFragmentManager(), "add_bookmark");
                            }
                        }
                    }
                });
            }
        });
    }
}

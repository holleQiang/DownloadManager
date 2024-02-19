package com.zhangqiang.web.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.web.activity.menu.MenuItemBean;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.fragment.HistoryFragment;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.utils.URLEncodeUtils;
import com.zhangqiang.web.webchromeclient.WebChromeClientImpl;
import com.zhangqiang.web.webviewclient.WebViewClientImpl;
import com.zhangqiang.webview.BuildConfig;
import com.zhangqiang.webview.R;
import com.zhangqiang.webview.databinding.ActivityWebViewBinding;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;


/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewActivity extends BaseActivity {

    private WebActivityContext mWebContext;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
    }

    private static final String INTENT_KEY_URL = "url";
    private static final String INTENT_KEY_SESSION_ID = "session_id";
    private ActivityWebViewBinding mActivityWebViewBinding;
    private HistoryFragment historyFragment;
    private List<MenuItemBean> currentMenuItems;

    public static Intent newIntent(Context context, String sessionId) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(INTENT_KEY_SESSION_ID, sessionId);
        return intent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mWebContext = (WebActivityContext) WebManager.getInstance().getWebContext(getIntent().getStringExtra(INTENT_KEY_SESSION_ID));
            if (mWebContext == null) {
                mWebContext = WebManager.getInstance().fromSystemOpen();
            }
        } else {
            mWebContext = WebManager.getInstance().fromActivityRestore(savedInstanceState.getString(INTENT_KEY_SESSION_ID),
                    savedInstanceState.getString(INTENT_KEY_URL));
        }

        mActivityWebViewBinding = ActivityWebViewBinding.inflate(getLayoutInflater());
        mWebContext.dispatchActivityCreate(this);
        setContentView(mActivityWebViewBinding.getRoot());
        setSupportActionBar(mActivityWebViewBinding.mToolBar);
        mActivityWebViewBinding.mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mActivityWebViewBinding.mWebView.setWebChromeClient(new WebChromeClientImpl(mWebContext,
                mActivityWebViewBinding.pbProgress,
                mActivityWebViewBinding.etTitle));
        mActivityWebViewBinding.mWebView.setWebViewClient(new WebViewClientImpl(this.mWebContext));
        WebSettings settings = mActivityWebViewBinding.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setSupportZoom(true);
        settings.setAllowFileAccessFromFileURLs(true);
        mWebContext.dispatchWebViewCreate(mActivityWebViewBinding.mWebView);

        mActivityWebViewBinding.etTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                invalidateOptionsMenu();
            }
        });
        mActivityWebViewBinding.etTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });
        mActivityWebViewBinding.etTitle.clearFocus();
        initHistoryFragment();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        int order = 0;
        if (mActivityWebViewBinding.etTitle.hasFocus()) {
            MenuItem searchMenuItem = menu.add(0, R.id.search_button, order++, R.string.search);
            searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            searchMenuItem.setIcon(R.drawable.ic_search_24);
        }

        List<MenuItemBean> menuItemBeans = mWebContext.getMenus();
        checkMenuItemId(menuItemBeans);
        for (MenuItemBean menuItemBean : menuItemBeans) {
            MenuItem menuItem = menu.add(0, menuItemBean.getId(), order++, menuItemBean.getTitle());
            menuItem.setShowAsAction(menuItemBean.getShowAsAction());
            menuItem.setIcon(menuItemBean.getIcon());
            List<MenuItemBean> subMenuItems = menuItemBean.getSubMenuItems();
            checkMenuItemId(subMenuItems);
            if (subMenuItems != null) {
                for (int i = 0; i < subMenuItems.size(); i++) {
                    MenuItemBean subMenuItem = subMenuItems.get(i);
                    SubMenu subMenu = menu.addSubMenu(menuItemBean.getId(), subMenuItem.getId(), Menu.NONE, subMenuItem.getTitle());
                    subMenuItem.setId(subMenu.getItem().getItemId());
                    subMenuItem.setIcon(subMenu.getItem().getIcon());
                }
            }
        }
        currentMenuItems = menuItemBeans;
        return super.onPrepareOptionsMenu(menu);
    }

    private static void checkMenuItemId(List<MenuItemBean> menuItemBeans) {
        if (menuItemBeans == null) {
            return;
        }
        SparseArray<MenuItemBean> sparseArray = new SparseArray<>();
        for (MenuItemBean menuItemBean : menuItemBeans) {
            if (sparseArray.get(menuItemBean.getId()) != null) {
                throw new IllegalStateException("duplicate menu item id");
            }
            sparseArray.put(menuItemBean.getId(), menuItemBean);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if (item.getItemId() == R.id.search_button) {
            performSearch();
            return true;
        }
        for (MenuItemBean menuItem : currentMenuItems) {
            if (menuItem.getId() == item.getItemId()) {
                mWebContext.dispatchMenuClick(menuItem);
                return true;
            }
            List<MenuItemBean> subMenuItems = menuItem.getSubMenuItems();
            if (subMenuItems != null) {
                for (MenuItemBean subMenuItem : subMenuItems) {
                    if (subMenuItem.getId() == item.getItemId()) {
                        mWebContext.dispatchMenuClick(subMenuItem);
                        return true;
                    }
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_KEY_SESSION_ID, mWebContext.getSessionId());
        outState.putString(INTENT_KEY_URL, mWebContext.getUrl());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityWebViewBinding.mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityWebViewBinding.mWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActivityWebViewBinding.mWebView.destroy();
        mWebContext.dispatchWebViewDestroy();
        mWebContext.dispatchActivityDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mActivityWebViewBinding.mWebView.canGoBack()) {
            mActivityWebViewBinding.mWebView.goBack();
            return;
        }
        if (historyFragment == null) {
            mActivityWebViewBinding.etTitle.setText(null);
            initHistoryFragment();
            return;
        }
        super.onBackPressed();
    }

    private void performSearch() {
        String input = mActivityWebViewBinding.etTitle.getText().toString().trim();
        performSearch(input);
    }

    private void initHistoryFragment() {
        historyFragment = new HistoryFragment();
        historyFragment.setOnVisitRecordClickListener(new HistoryFragment.OnVisitRecordClickListener() {
            @Override
            public void onVisitRecordClick(VisitRecordBean visitRecordBean) {
                mActivityWebViewBinding.etTitle.setText(visitRecordBean.getUrl());
                performSearch();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, historyFragment)
                .commit();
    }

    public void performSearch(String input) {
        boolean isLink = false;
        if (!TextUtils.isEmpty(input)) {
            Uri inputUri = Uri.parse(input);
            String scheme = inputUri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                isLink = true;
            } else if ("chls.pro/ssl".equalsIgnoreCase(input)) {
                isLink = true;
            } else if (input.endsWith(".com") || input.endsWith(".net") || input.endsWith(".cc")) {
                isLink = true;
            }
        }
        String loadUrl;
        if (isLink) {
            loadUrl = input;
        } else {
            String params = URLEncodeUtils.encodeUrl(input);
            if (!TextUtils.isEmpty(params)) {
                loadUrl = "https://www.baidu.com/s?wd=" + params;
            } else {
                loadUrl = "https://www.baidu.com";
            }
        }
        mActivityWebViewBinding.mWebView.loadUrl(loadUrl);
        mWebContext.dispatchLoadUrl(loadUrl);

        if (historyFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(historyFragment)
                    .commit();
            historyFragment = null;
        }
        mActivityWebViewBinding.etTitle.clearFocus();

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(mActivityWebViewBinding.etTitle.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}

package com.zhangqiang.web.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
        } else {
            mWebContext = WebManager.getInstance().fromActivityRestore(savedInstanceState.getString(INTENT_KEY_SESSION_ID),
                    savedInstanceState.getString(INTENT_KEY_URL));
        }
        if (mWebContext == null) {
            throw new IllegalArgumentException("webContext error");
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
//        mActivityWebViewBinding.mToolBar.setna(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });
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
        settings.setUseWideViewPort(true);
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

        if(mActivityWebViewBinding.etTitle.hasFocus()){
            MenuItem searchMenuItem = menu.add(0,R.id.search_button,0,R.string.search);
            searchMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            searchMenuItem.setIcon(R.drawable.ic_search_24);
        }

        List<MenuItemBean> menus = mWebContext.getMenus();
        for (MenuItemBean menuItemBean : menus) {
            MenuItem menuItem = menu.add(menuItemBean.getTitle());
            int itemId = menuItem.getItemId();
            menuItemBean.setId(itemId);
            menuItem.setShowAsAction(menuItemBean.getShowAsAction());
            List<MenuItemBean> subMenuItems = menuItemBean.getSubMenuItems();
            if (subMenuItems != null) {
                for (MenuItemBean subMenuItem : subMenuItems) {
                    SubMenu subMenu = menu.addSubMenu(itemId, itemId, Menu.NONE, subMenuItem.getTitle());
                    int itemId1 = subMenu.getItem().getItemId();
                }
            }
        }
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            android.view.MenuItem menuItem = menu.getItem(i);
            int itemId = menuItem.getItemId();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull android.view.MenuItem item) {
        if(item.getItemId() == R.id.search_button){
            performSearch();
            return true;
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
        super.onBackPressed();
    }

    private void performSearch() {
        String input = mActivityWebViewBinding.etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        Uri inputUri = Uri.parse(input);
        String scheme = inputUri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            mActivityWebViewBinding.mWebView.loadUrl(input);
            mWebContext.dispatchLoadUrl(input);
        } else {
            try {
                String url = "https://www.baidu.com/s?wd=" + URLEncoder.encode(input, "utf-8");
                mActivityWebViewBinding.mWebView.loadUrl(url);
                mWebContext.dispatchLoadUrl(url);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        if (historyFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(historyFragment)
                    .commit();
            historyFragment = null;
        }
        mActivityWebViewBinding.etTitle.clearFocus();
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
}

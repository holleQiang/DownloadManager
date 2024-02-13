package com.zhangqiang.web.resource.collect.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.web.resource.collect.fragment.ResourceListFragment;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

public class ResourceLookupDialog extends BaseDialogFragment {

    private static final String KEY_SESSION_ID = "session_id";
    private String sessionId;

    public static ResourceLookupDialog newInstance(String sessionId) {
        ResourceLookupDialog dialog = new ResourceLookupDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SESSION_ID, sessionId);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            sessionId = arguments.getString(KEY_SESSION_ID);
        }
        if (TextUtils.isEmpty(sessionId)) {
            throw new IllegalArgumentException("session id cannot be null");
        }
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected float getHeightRatio() {
        return 0.75f;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_web_resource_collect;
    }

    @Override
    protected void initView(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.vp_detail);
        TabLayout tabLayout = view.findViewById(R.id.tl_category);

        List<TabFeedBean> tabFeeds = new ArrayList<>();
        tabFeeds.add(new TabFeedBean(getString(R.string.all), ResourceListFragment.CATEGORY_ALL));
        tabFeeds.add(new TabFeedBean(getString(R.string.image), ResourceListFragment.CATEGORY_IMAGE));
        tabFeeds.add(new TabFeedBean(getString(R.string.video), ResourceListFragment.CATEGORY_VIDEO));
        tabFeeds.add(new TabFeedBean(getString(R.string.css), ResourceListFragment.CATEGORY_CSS));
        tabFeeds.add(new TabFeedBean(getString(R.string.music), ResourceListFragment.CATEGORY_AUDIO));
        viewPager.setAdapter(new ResourceLookupViewPagerAdapter(this, sessionId, tabFeeds));
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabFeeds.get(position).getTabTitle());
            }
        }).attach();
    }
}

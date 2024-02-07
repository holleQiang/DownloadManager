package com.zhangqiang.web.resource.collect.dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zhangqiang.web.resource.collect.fragment.ResourceListFragment;

import java.util.List;

public class ResourceLookupViewPagerAdapter extends FragmentStateAdapter {

    private final String sessionId;
    private final List<TabFeedBean> tabFeeds;

    public ResourceLookupViewPagerAdapter(@NonNull Fragment fragment, String sessionId, List<TabFeedBean> tabFeeds) {
        super(fragment);
        this.sessionId = sessionId;
        this.tabFeeds = tabFeeds;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        TabFeedBean tabFeedBean = tabFeeds.get(position);
        return ResourceListFragment.newInstance(sessionId, tabFeedBean.getCategory());
    }

    @Override
    public int getItemCount() {
        return tabFeeds.size();
    }
}

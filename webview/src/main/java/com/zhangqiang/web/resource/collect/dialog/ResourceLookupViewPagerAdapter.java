package com.zhangqiang.web.resource.collect.dialog;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zhangqiang.web.resource.collect.fragment.ResourceListFragment;

import java.util.ArrayList;
import java.util.List;

public class ResourceLookupViewPagerAdapter extends FragmentStateAdapter {

    private final List<TabFeedBean> tabFeeds = new ArrayList<>();


    public ResourceLookupViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
        tabFeeds.add(new TabFeedBean());
        tabFeeds.add(new TabFeedBean());
        tabFeeds.add(new TabFeedBean());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ResourceListFragment.newInstance(1);
    }

    @Override
    public int getItemCount() {
        return tabFeeds.size();
    }
}

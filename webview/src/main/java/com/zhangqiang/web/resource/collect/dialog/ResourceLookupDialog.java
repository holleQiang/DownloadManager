package com.zhangqiang.web.resource.collect.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.webview.R;

public class ResourceLookupDialog extends BaseDialogFragment {

    private static final String KEY_SESSION_ID = "session_id";

    public static ResourceLookupDialog newInstance(String sessionId){
        ResourceLookupDialog dialog = new ResourceLookupDialog();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_SESSION_ID,sessionId);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            String sessionId = arguments.getString(KEY_SESSION_ID);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_web_resource_collect;
    }

    @Override
    protected void initView(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.vp_detail);
        TabLayout tabLayout = view.findViewById(R.id.tl_category);
        viewPager.setAdapter(new ResourceLookupViewPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(""+position);
            }
        }).attach();
    }
}

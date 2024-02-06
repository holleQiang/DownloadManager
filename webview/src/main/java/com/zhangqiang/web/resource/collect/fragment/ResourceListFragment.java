package com.zhangqiang.web.resource.collect.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.common.fragment.BaseFragment;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.webview.R;

public class ResourceListFragment extends BaseFragment {


    private static final int CATEGORY_ALL = 0;
    private static final String KEY_CATEGORY = "category";

    public static ResourceListFragment newInstance(int category){
        ResourceListFragment resourceListFragment = new ResourceListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_CATEGORY,category);
        resourceListFragment.setArguments(arguments);
        return resourceListFragment;
    }

    private CellRVAdapter resourceListAdapter;
    private int category;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            category = arguments.getInt(KEY_CATEGORY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resource_list,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvResourceList = view.findViewById(R.id.rv_resource_list);
        rvResourceList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        resourceListAdapter = new CellRVAdapter();
        rvResourceList.setAdapter(resourceListAdapter);

    }
}

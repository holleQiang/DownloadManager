package com.zhangqiang.web.resource.collect.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.common.fragment.BaseFragment;
import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.ClipboardUtils;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.web.context.OnLoadResourceListener;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.resource.collect.OnResourceChangeListener;
import com.zhangqiang.web.resource.collect.ResourceCollectPlugin;
import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.fragment.cell.ResourceBean;
import com.zhangqiang.web.resource.collect.fragment.cell.ResourceCell;
import com.zhangqiang.web.resource.collect.options.OnOptionClickListener;
import com.zhangqiang.web.resource.collect.options.Option;
import com.zhangqiang.web.resource.collect.options.OptionsDialog;
import com.zhangqiang.web.resource.collect.tabs.TabProvider;
import com.zhangqiang.web.resource.collect.tabs.TabProviders;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ResourceListFragment extends BaseFragment {

    private static final String KET_PROVIDER_ID = "provider_id";
    private static final String KEY_SESSION_ID = "session_id";
    private CellRVAdapter resourceListAdapter;
    private ResourceCollectPlugin resourceCollectPlugin;
    private RecyclerView rvResourceList;

    public static ResourceListFragment newInstance(String sessionId, int providerId) {
        ResourceListFragment resourceListFragment = new ResourceListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(KET_PROVIDER_ID, providerId);
        arguments.putString(KEY_SESSION_ID, sessionId);
        resourceListFragment.setArguments(arguments);
        return resourceListFragment;
    }

    private TabProvider tabProvider;
    private String sessionId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            int providerId = arguments.getInt(KET_PROVIDER_ID);
            tabProvider = TabProviders.get(getContext()).findById(providerId);
            if (tabProvider == null) {
                throw new NullPointerException("tab provider is null");
            }
            sessionId = arguments.getString(KEY_SESSION_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resource_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvResourceList = view.findViewById(R.id.rv_resource_list);
        rvResourceList.setLayoutManager(new LinearLayoutManager(view.getContext()) {
            @Override
            public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
                int vertically = super.scrollVerticallyBy(dy, recycler, state);
                return vertically == 0 ? 1 : vertically;
            }
        });
        resourceListAdapter = new CellRVAdapter();
        rvResourceList.setAdapter(resourceListAdapter);

        resourceCollectPlugin = (ResourceCollectPlugin) WebManager.getInstance().findPluginOrThrow(new WebManager.Filter() {
            @Override
            public boolean onFilter(WebPlugin plugin) {
                return plugin instanceof ResourceCollectPlugin;
            }
        });
        List<WebResource> resourceList = resourceCollectPlugin.getResourceList(sessionId);
        if (resourceList == null) {
            return;
        }
        Observable.just(resourceList)
                .compose(RXJavaUtils.applyIOMainSchedules())
                .map(new Function<List<WebResource>, List<ResourceCell>>() {
                    @Override
                    public List<ResourceCell> apply(List<WebResource> webResources) throws Exception {
                        List<WebResource> newList = new ArrayList<>();
                        for (WebResource webResource : webResources) {
                            if(tabProvider.isTargetResource(webResource)){
                                newList.add(webResource);
                            }
                        }
                        return convertToCell(convertToBeans(newList));
                    }
                })
                .compose(RXJavaUtils.bindLifecycle(this))
                .subscribe(new BaseObserver<List<ResourceCell>>() {
                    @Override
                    public void onNext(List<ResourceCell> resourceCells) {
                        resourceListAdapter.setDataList(resourceCells);
                    }
                });

        resourceCollectPlugin.addOnResourceChangeListener(onResourceChangeListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        resourceCollectPlugin.removeOnResourceChangeListener(onResourceChangeListener);
    }

    private final OnResourceChangeListener onResourceChangeListener = new OnResourceChangeListener() {
        @Override
        public void onLoadWebResource(String sessionId, WebResource webResource) {
            if (tabProvider.isTargetResource(webResource)) {
                resourceListAdapter.addDataAtLast(new ResourceCell(convertToBean(webResource)));
                if (isLastItemCompletelyVisible()) {
                    rvResourceList.scrollToPosition(resourceListAdapter.getItemCount() - 1);
                }
            }
        }
    };

    private boolean isLastItemCompletelyVisible() {

        LinearLayoutManager layoutManager = (LinearLayoutManager) rvResourceList.getLayoutManager();
        if (layoutManager == null || resourceListAdapter == null) {
            return false;
        }
        int lastCompletelyVisibleItemPosition = layoutManager.findLastCompletelyVisibleItemPosition();
        return lastCompletelyVisibleItemPosition == resourceListAdapter.getItemCount() - 1;
    }

    private ResourceBean convertToBean(WebResource webResource) {
        String url = webResource.getUrl();
        Uri uri = Uri.parse(url);
        String path = uri.getLastPathSegment();
        String title = null;
        if (!TextUtils.isEmpty(path)) {
            int index = url.indexOf(path);
            if (index != -1) {
                title = url.substring(index);
            }
        }
        if (TextUtils.isEmpty(title)) {
            title = url;
        }
        return new ResourceBean(url, title);
    }

    private List<ResourceBean> convertToBeans(List<WebResource> webResources) {
        if (webResources == null) {
            return null;
        }
        List<ResourceBean> resourceBeans = new ArrayList<>();
        for (WebResource webResource : webResources) {
            resourceBeans.add(convertToBean(webResource));
        }
        return resourceBeans;
    }

    private List<ResourceCell> convertToCell(List<ResourceBean> resources) {
        if (resources == null) {
            return null;
        }
        List<ResourceCell> cells = new ArrayList<>();
        for (ResourceBean resource : resources) {
            ResourceCell resourceCell = new ResourceCell(resource);
            resourceCell.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick() {
                    new OptionsDialog().setOnOptionClickListener(new OnOptionClickListener() {
                        @Override
                        public void onOptionClick(Option option) {
                            Context context = getContext();
                            if (context != null) {
                                ClipboardUtils.copy(context, resource.getUrl());
                                Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show(getChildFragmentManager(), "options");
                }
            });
            cells.add(resourceCell);
        }
        return cells;
    }
}

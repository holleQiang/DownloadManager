package com.zhangqiang.web.resource.collect.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.resource.collect.ResourceCollectPlugin;
import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.web.resource.collect.fragment.cell.ResourceBean;
import com.zhangqiang.web.resource.collect.fragment.cell.ResourceCell;
import com.zhangqiang.web.resource.collect.options.OnOptionClickListener;
import com.zhangqiang.web.resource.collect.options.Option;
import com.zhangqiang.web.resource.collect.options.OptionsDialog;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class ResourceListFragment extends BaseFragment {

    public static final String IMAGE_PATTERN = ".*\\.((png)|(jpg)|(jpeg)|(webp)|(gif))$";

    public static final int CATEGORY_ALL = 0;
    public static final int CATEGORY_IMAGE = 1;
    public static final int CATEGORY_VIDEO = 2;
    public static final int CATEGORY_CSS = 3;
    public static final int CATEGORY_AUDIO = 4;
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_SESSION_ID = "session_id";

    public static ResourceListFragment newInstance(String sessionId, int category) {
        ResourceListFragment resourceListFragment = new ResourceListFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(KEY_CATEGORY, category);
        arguments.putString(KEY_SESSION_ID, sessionId);
        resourceListFragment.setArguments(arguments);
        return resourceListFragment;
    }

    private int category;
    private String sessionId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            category = arguments.getInt(KEY_CATEGORY);
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
        RecyclerView rvResourceList = view.findViewById(R.id.rv_resource_list);
        rvResourceList.setLayoutManager(new LinearLayoutManager(view.getContext()));
        CellRVAdapter resourceListAdapter = new CellRVAdapter();
        rvResourceList.setAdapter(resourceListAdapter);

        ResourceCollectPlugin resourceCollectPlugin = (ResourceCollectPlugin) WebManager.getInstance().findPluginOrThrow(new WebManager.Filter() {
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
                        String reg = null;
                        if (category == CATEGORY_IMAGE) {
                            reg = IMAGE_PATTERN;
                        } else if (category == CATEGORY_VIDEO) {
                            reg = ".*\\.((m3u8)|(mp4)|(flv)|(mkv)|(avi))$";
                        } else if (category == CATEGORY_AUDIO) {
                            reg = ".*\\.(mp3)$";
                        } else if (category == CATEGORY_CSS) {
                            reg = ".*\\.(css)$";
                        }
                        if (reg != null) {
                            List<WebResource> newList = new ArrayList<>();
                            for (WebResource webResource : webResources) {
                                String url = webResource.getUrl();
                                Uri uri = Uri.parse(url);
                                String path = uri.getPath();
                                if (Pattern.compile(reg).matcher(path).matches()) {
                                    newList.add(webResource);
                                }
                            }
                            return convertToCell(convertToBean(newList));
                        }
                        return convertToCell(convertToBean(webResources));
                    }
                })
                .compose(RXJavaUtils.bindLifecycle(this))
                .subscribe(new BaseObserver<List<ResourceCell>>() {
                    @Override
                    public void onNext(List<ResourceCell> resourceCells) {
                        resourceListAdapter.setDataList(resourceCells);
                    }
                });
    }

    private List<ResourceBean> convertToBean(List<WebResource> webResources) {
        if (webResources == null) {
            return null;
        }
        List<ResourceBean> resourceBeans = new ArrayList<>();
        for (WebResource webResource : webResources) {
            String url = webResource.getUrl();
            Uri uri = Uri.parse(url);
            String path = uri.getLastPathSegment();
            int index = url.indexOf(path);
            String title = path;
            if (index != -1) {
                title = url.substring(index);
            }
            resourceBeans.add(new ResourceBean(url, title));
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
                                ClipboardUtils.copy(context,resource.getUrl());
                                Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).show(getChildFragmentManager(),"options");
                }
            });
            cells.add(resourceCell);
        }
        return cells;
    }
}

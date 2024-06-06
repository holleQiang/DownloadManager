package com.zhangqiang.web.spider.dialog;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.WebPlugin;
import com.zhangqiang.web.spider.SpiderPlugin;
import com.zhangqiang.web.spider.bean.SpiderBean;
import com.zhangqiang.web.spider.cell.SpiderCell;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

public class SpiderDialog extends BaseDialogFragment {

    public interface OnSpiderClickListener{
        void onSpiderClick(SpiderBean spiderBean, int position);
    }

    public static SpiderDialog newInstance() {
        return new SpiderDialog();
    }

    private OnSpiderClickListener onSpiderClickListener;

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_spider;
    }

    @Override
    protected void initView(View view) {
        RecyclerView rvSpider = view.findViewById(R.id.rv_spider);
        rvSpider.setLayoutManager(new LinearLayoutManager(view.getContext()));
        CellRVAdapter adapter = new CellRVAdapter();
        rvSpider.setAdapter(adapter);

        SpiderPlugin spiderPlugin = (SpiderPlugin) WebManager.getInstance().findPluginOrThrow(new WebManager.Filter() {
            @Override
            public boolean onFilter(WebPlugin plugin) {
                return plugin instanceof SpiderPlugin;
            }
        });
        List<SpiderBean> spiders = spiderPlugin.getSpiders();
        ArrayList<Cell> dataList = new ArrayList<>();
        for (SpiderBean spider : spiders) {
            dataList.add(new SpiderCell(spider,onSpiderClickListener));
        }
        adapter.setDataList(dataList);
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected float getHeightRatio() {
        return 0.75f;
    }

    public void setOnSpiderClickListener(OnSpiderClickListener onSpiderClickListener) {
        this.onSpiderClickListener = onSpiderClickListener;
    }
}

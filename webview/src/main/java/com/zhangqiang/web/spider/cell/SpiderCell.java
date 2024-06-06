package com.zhangqiang.web.spider.cell;

import android.view.View;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.spider.bean.SpiderBean;
import com.zhangqiang.web.spider.dialog.SpiderDialog;
import com.zhangqiang.webview.R;

public class SpiderCell extends MultiCell<SpiderBean> {

    private final SpiderDialog.OnSpiderClickListener onSpiderClickListener;

    public SpiderCell(SpiderBean data, SpiderDialog.OnSpiderClickListener onSpiderClickListener) {
        super(R.layout.item_spider, data, null);
        this.onSpiderClickListener = onSpiderClickListener;
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        vh.setText(R.id.tv_title,getData().getName());
        vh.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onSpiderClickListener != null) {
                    onSpiderClickListener.onSpiderClick(getData(),vh.getAdapterPosition());
                }
            }
        });
    }
}

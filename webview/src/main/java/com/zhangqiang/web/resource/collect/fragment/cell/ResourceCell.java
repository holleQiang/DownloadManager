package com.zhangqiang.web.resource.collect.fragment.cell;

import android.view.View;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.webview.R;

public class ResourceCell extends MultiCell<ResourceBean> {
    public ResourceCell(ResourceBean data) {
        super(R.layout.item_web_resource, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        vh.setText(R.id.tv_title, getData().getTitle());
        View view = vh.getView();
        view.setBackgroundResource(vh.getAdapterPosition() % 2 == 0 ? R.color.resource_item_single : R.color.resource_item_double);
    }
}

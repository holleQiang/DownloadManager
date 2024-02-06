package com.zhangqiang.web.resource.collect.fragment.cell;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.resource.collect.bean.WebResource;
import com.zhangqiang.webview.R;

public class ResourceCell extends MultiCell<WebResource> {
    public ResourceCell(WebResource data) {
        super(R.layout.item_web_resource, data,null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        vh.setText(R.id.tv_title,getData().getUrl());
    }
}

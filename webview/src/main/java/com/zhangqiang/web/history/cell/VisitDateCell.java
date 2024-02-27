package com.zhangqiang.web.history.cell;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.webview.R;

public class VisitDateCell extends MultiCell<String> {

    public VisitDateCell(String data) {
        super(R.layout.item_visit_date, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        vh.setText(R.id.tv_title, getData());
    }
}

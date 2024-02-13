package com.zhangqiang.web.boomark.cell;

import android.view.View;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.boomark.bean.BookMarkBean;
import com.zhangqiang.webview.R;

public class BookMarkCell extends MultiCell<BookMarkBean> {

    private View.OnClickListener onClickListener;

    public BookMarkCell(BookMarkBean data) {
        super(R.layout.item_bookmark, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        vh.setText(R.id.tv_title, getData().getTitle());
        vh.getView().setOnClickListener(onClickListener);
    }

    public BookMarkCell setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
}

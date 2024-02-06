package com.zhangqiang.web.history.cell;

import android.text.TextUtils;
import android.view.View;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.webview.R;

public class VisitRecordCell extends MultiCell<VisitRecordBean> {

    public interface OnVisitRecordLongClickListener{
        void onVisitRecordLongClick(VisitRecordBean data,int position);
    }

    private final View.OnClickListener onClickListener;
    private final OnVisitRecordLongClickListener onVisitRecordLongClickListener;


    public VisitRecordCell(VisitRecordBean data, View.OnClickListener onClickListener,OnVisitRecordLongClickListener onVisitRecordLongClickListener) {
        super(R.layout.item_visit_record, data, null);
        this.onClickListener = onClickListener;
        this.onVisitRecordLongClickListener = onVisitRecordLongClickListener;
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        VisitRecordBean data = getData();
        vh.setImageBitmap(R.id.iv_icon,data.getIcon());
        String title = data.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = data.getUrl();
        }
        vh.setText(R.id.tv_title, title);
        vh.getView().setOnClickListener(onClickListener);
        vh.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onVisitRecordLongClickListener.onVisitRecordLongClick(data,vh.getAdapterPosition());
                return true;
            }
        });
    }
}

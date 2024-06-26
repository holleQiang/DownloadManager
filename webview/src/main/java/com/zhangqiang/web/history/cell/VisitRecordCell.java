package com.zhangqiang.web.history.cell;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.webview.R;

public class VisitRecordCell extends MultiCell<VisitRecordBean> {

    public interface OnVisitRecordLongClickListener {
        void onVisitRecordLongClick(VisitRecordBean data, int position);
    }

    private final OnVisitRecordClickListener onClickListener;
    private final OnVisitRecordLongClickListener onVisitRecordLongClickListener;


    public VisitRecordCell(VisitRecordBean data, OnVisitRecordClickListener onClickListener, OnVisitRecordLongClickListener onVisitRecordLongClickListener) {
        super(R.layout.item_visit_record, data, null);
        this.onClickListener = onClickListener;
        this.onVisitRecordLongClickListener = onVisitRecordLongClickListener;
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        VisitRecordBean data = getData();
        Glide.with(vh.getView().getContext()).load(data.getIconUrl()).into((ImageView) vh.getView(R.id.iv_icon));
        String title = data.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = data.getUrl();
        }
        vh.setText(R.id.tv_title, title);
        vh.getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.onVisitRecordClick(data,vh.getAdapterPosition());
            }
        });
        vh.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onVisitRecordLongClickListener.onVisitRecordLongClick(data, vh.getAdapterPosition());
                return true;
            }
        });
    }
}

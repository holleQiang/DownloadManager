package com.zhangqiang.web.resource.collect.fragment.cell;

import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.web.resource.collect.fragment.OnItemClickListener;
import com.zhangqiang.web.resource.collect.utils.Utils;
import com.zhangqiang.webview.R;

public class ResourceCell extends MultiCell<ResourceBean> {

    private OnItemClickListener onItemClickListener;

    public ResourceCell(ResourceBean data) {
        super(R.layout.item_web_resource, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        ResourceBean data = getData();
        vh.setText(R.id.tv_title, data.getTitle());
        View view = vh.getView();
        view.setBackgroundResource(vh.getAdapterPosition() % 2 == 0 ? R.color.resource_item_single : R.color.resource_item_double);
        String url = data.getUrl();
        if (Utils.isImageUrl(url)) {
            Glide.with(view.getContext()).load(url).into((ImageView) vh.getView(R.id.iv_icon));
        } else {
            vh.setImageResource(R.id.iv_icon, R.drawable.ic_file);
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick();
                }
            }
        });
    }

    public ResourceCell setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }
}

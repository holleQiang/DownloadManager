package com.zhangqiang.sample.ui.widget;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class LinearRVDivider extends RecyclerView.ItemDecoration {

    private final Drawable mDividerDrawable;

    public LinearRVDivider(@NonNull Drawable dividerDrawable) {
        this.mDividerDrawable = dividerDrawable;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(view);
        int adapterPosition = viewHolder.getAdapterPosition();
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof GridLayoutManager)
                && layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            int itemCount = linearLayoutManager.getItemCount();
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                if (shouldDrawDivider(adapterPosition, itemCount)) {
                    outRect.set(0, 0, mDividerDrawable.getBounds().width(), 0);
                }
            } else {
                if (shouldDrawDivider(adapterPosition, itemCount)) {
                    outRect.set(0, 0, 0, mDividerDrawable.getBounds().height());
                }
            }
        }
    }

    private boolean shouldDrawDivider(int adapterPosition, int itemCount) {
        return adapterPosition >= 0 && adapterPosition != itemCount - 1 && itemCount > 1;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (!(layoutManager instanceof GridLayoutManager) && layoutManager instanceof LinearLayoutManager) {
            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            Rect bounds = mDividerDrawable.getBounds();
            int orientation = linearLayoutManager.getOrientation();
            if (orientation == LinearLayoutManager.HORIZONTAL) {
                mDividerDrawable.setBounds(bounds.left,
                        bounds.top + parent.getPaddingTop(),
                        bounds.right,
                        bounds.bottom + parent.getHeight() - parent.getPaddingBottom());
            } else {
                mDividerDrawable.setBounds(bounds.left + parent.getPaddingTop(),
                        bounds.top,
                        bounds.right + parent.getWidth() - parent.getPaddingRight(),
                        bounds.bottom);
            }
            int itemCount = linearLayoutManager.getItemCount();
            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                RecyclerView.ViewHolder viewHolder = parent.getChildViewHolder(child);
                int adapterPosition = viewHolder.getAdapterPosition();
                if (!shouldDrawDivider(adapterPosition, itemCount)) {
                    continue;
                }
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    int right = child.getRight();
                    bounds.offsetTo(right, 0);
                    mDividerDrawable.setBounds(bounds);
                } else {
                    int bottom = child.getBottom();
                    bounds.offsetTo(0, bottom);
                    mDividerDrawable.setBounds(bounds);
                }
                mDividerDrawable.draw(c);
            }
        }
    }
}

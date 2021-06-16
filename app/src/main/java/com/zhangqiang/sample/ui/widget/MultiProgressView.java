package com.zhangqiang.sample.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class MultiProgressView extends View {

    private final List<ProgressEntry> progressEntries = new ArrayList<>();
    private int totalMax;
    private final Paint mPaint = new Paint();

    public MultiProgressView(Context context) {
        super(context);
        init();
    }


    public MultiProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MultiProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (totalMax == 0) {
            return;
        }
        int availableWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        int availableHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right;
        int bottom = top + availableHeight;
        int totalProgress = 0;
        for (int i = 0; i < progressEntries.size(); i++) {
            ProgressEntry progressEntry = progressEntries.get(i);
            totalProgress += progressEntry.progress;
            float widthFactor = (float) progressEntry.max / totalMax;
            int regionWidth = (int) (availableWidth * widthFactor);
            float progressFactor = (float) progressEntry.progress / progressEntry.max;
            int progressWidth = (int) (progressFactor * regionWidth);
            mPaint.setColor(progressEntry.color);
            if (totalProgress == totalMax) {
                right = getWidth() - getPaddingRight();
            } else {
                right = left + progressWidth;
            }
            canvas.drawRect(left, top, right, bottom, mPaint);
            String progressInfo = progressEntry.info;
            if (!TextUtils.isEmpty(progressInfo)) {
                float textWidth = mPaint.measureText(progressInfo);
                if (textWidth < progressWidth) {
                    mPaint.setColor(Color.WHITE);
                    float fTop = (bottom - top - mPaint.descent() + mPaint.ascent()) / 2 - mPaint.ascent();
                    float fLeft = (right - left - textWidth) / 2 + left;
                    canvas.drawText(progressInfo, fLeft, fTop, mPaint);
                }
            }
            left = right;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int textHeight = (int) Math.ceil(mPaint.descent() - mPaint.ascent());
        int height = textHeight + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(0,widthMeasureSpec),resolveSize(height,heightMeasureSpec));
    }

    static class ProgressEntry {

        private final int id;
        private int progress;
        private final int max;
        private final int color;
        private final String info;

        ProgressEntry(int id, int progress, int max, int color, String info) {
            this.id = id;
            this.progress = progress;
            this.max = max;
            this.color = color;
            this.info = info;
        }
    }

    public void addProgressEntry(int id, int progress, int max, int color, String info) {
        if (max <= 0) {
            throw new IllegalArgumentException("max cannot be smaller than 0");
        }
        progress = Math.min(max, progress);
        progressEntries.add(new ProgressEntry(id, progress, max, color, info));
        totalMax += max;
        invalidate();
    }

    public void updateProgress(int id, int progress) {
        for (ProgressEntry entry : progressEntries) {
            if (entry.id == id && entry.progress != progress) {
                entry.progress = Math.min(entry.max, progress);
                invalidate();
            }
        }
    }

    public void removeProgress(int id) {
        for (int i = progressEntries.size() - 1; i >= 0; i--) {
            if (progressEntries.get(i).id == id) {
                progressEntries.remove(i);
            }
        }
    }

    public void clear(){
        boolean empty = progressEntries.isEmpty();
        progressEntries.clear();
        totalMax = 0;
        if (!empty) {
            invalidate();
        }
    }

    private void init() {
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
        if (isInEditMode()) {
            addProgressEntry(0, 100, 100, Color.RED, "red info");
            addProgressEntry(1, 50, 50, Color.BLUE, "blue info");
            addProgressEntry(2, 70, 70, Color.YELLOW, "yellow info");
        }

    }
}

package com.zhangqiang.sample.ui.cell.icon;

import android.widget.ImageView;

import java.io.File;

public interface BackgroundProvider {
    void loadBackground(ImageView view, File file);
}

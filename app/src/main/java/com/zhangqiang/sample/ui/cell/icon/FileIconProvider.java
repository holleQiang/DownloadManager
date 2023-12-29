package com.zhangqiang.sample.ui.cell.icon;

import android.widget.ImageView;

import java.io.File;

public interface FileIconProvider {

    int defaultIconResource();

    void loadFileIcon(ImageView imageView, File file);
}

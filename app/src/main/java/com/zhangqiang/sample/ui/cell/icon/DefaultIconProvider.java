package com.zhangqiang.sample.ui.cell.icon;

import android.widget.ImageView;

import com.zhangqiang.sample.R;

import java.io.File;

public class DefaultIconProvider implements FileIconProvider {


    @Override
    public void loadFileIcon(ImageView imageView, File file) {
        imageView.setImageResource(R.mipmap.ic_file_default);
    }


    @Override
    public int defaultIconResource() {
        return R.mipmap.ic_file_default;
    }
}

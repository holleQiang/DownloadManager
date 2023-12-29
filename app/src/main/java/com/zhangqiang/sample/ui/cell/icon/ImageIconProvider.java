package com.zhangqiang.sample.ui.cell.icon;

import android.widget.ImageView;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.utils.ImageUtils;

import java.io.File;

public class ImageIconProvider implements FileIconProvider{
    @Override
    public int defaultIconResource() {
        return R.mipmap.ic_image_default;
    }

    @Override
    public void loadFileIcon(ImageView imageView, File file) {
        ImageUtils.loadImageFromFile(imageView,file);
    }

}

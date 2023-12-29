package com.zhangqiang.sample.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

public class ImageUtils {

    public static void loadImageFromFile(ImageView imageView, File file){
        Glide.with(imageView.getContext()).load(file).into(imageView);
    }
}

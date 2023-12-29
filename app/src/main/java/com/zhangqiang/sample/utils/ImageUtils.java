package com.zhangqiang.sample.utils;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class ImageUtils {

    public static void loadImageFromFile(ImageView imageView, File file) {
        Glide.with(imageView.getContext()).load(file).into(imageView);
    }

    public static void loadBlurImageFromFile(ImageView view, File file) {
        Glide.with(view.getContext()).load(file)
                .apply(new RequestOptions().transform(new BlurTransformation(20,5)))
                .into(view);
    }
}

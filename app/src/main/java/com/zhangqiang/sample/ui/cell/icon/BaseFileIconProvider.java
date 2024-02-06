package com.zhangqiang.sample.ui.cell.icon;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.lifecycle.LifecycleOwner;


import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public abstract class BaseFileIconProvider implements FileIconProvider{

    private final LifecycleOwner lifecycleOwner;

    public BaseFileIconProvider(LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
    }

    public final void loadFileIcon(ImageView imageView, File file) {
        Observable.just(file)
                .map(new Function<File, Drawable>() {
                    @Override
                    public Drawable apply(File file) throws Exception {
                        return getFileIcon(file);
                    }
                })
                .compose(RXJavaUtils.bindLifecycle(lifecycleOwner))
                .compose(RXJavaUtils.applyIOMainSchedules()).subscribe(new BaseObserver<Drawable>() {
                    @Override
                    public void onNext(Drawable drawable) {
                        imageView.setImageDrawable(drawable);
                    }
                });
    }

    protected abstract Drawable getFileIcon(File file);
}

package com.zhangqiang.sample.ui.cell.icon;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.lifecycle.LifecycleOwner;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.utils.PackageUtils;

import java.io.File;

public class ApkIconProvider extends BaseFileIconProvider {

    private final Context context;

    public ApkIconProvider(LifecycleOwner lifecycleOwner, Context context) {
        super(lifecycleOwner);
        this.context = context;
    }

    @Override
    public int defaultIconResource() {
        return R.mipmap.ic_apk_default;
    }

    @Override
    protected Drawable getFileIcon(File file) {
        PackageUtils.PackageInfoBean packageInfo = PackageUtils.getPackageInfo(context, file.getAbsolutePath());
        if (packageInfo != null) {
            return packageInfo.getAppIcon();
        }
        return null;
    }
}

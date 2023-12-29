package com.zhangqiang.sample.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class PackageUtils {

    public static class PackageInfoBean {
        private final String appName;
        private final Drawable appIcon;
        private final String packageName;
        private final String versionName;
        private final int versionCode;

        PackageInfoBean(String appName, Drawable appIcon, String packageName, String versionName, int versionCode) {
            this.appName = appName;
            this.appIcon = appIcon;
            this.packageName = packageName;
            this.versionName = versionName;
            this.versionCode = versionCode;
        }

        public String getAppName() {
            return appName;
        }

        public Drawable getAppIcon() {
            return appIcon;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getVersionName() {
            return versionName;
        }

        public int getVersionCode() {
            return versionCode;
        }
    }

    public static PackageInfoBean getPackageInfo(Context context, String filepath) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(filepath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            applicationInfo.sourceDir = filepath;
            applicationInfo.publicSourceDir = filepath;
            String appName = packageManager.getApplicationLabel(applicationInfo).toString();
            String packageName = packageInfo.packageName;
            String versionName = packageInfo.versionName;
            int versionCode = packageInfo.versionCode;
            Drawable appIcon = packageManager.getApplicationIcon(applicationInfo);
            return new PackageInfoBean(appName, appIcon, packageName, versionName, versionCode);
        }
        return null;
    }
}

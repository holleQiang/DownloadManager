package com.zhangqiang.sample.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;

import java.util.regex.Pattern;

public class QRCodeResultProcessUtils {

    public static void processHttpUrl(AppCompatActivity activity, String url) {
        if (Pattern.compile("\\.apk").matcher(url).find()) {
            CreateTaskDialog.createAndShow(activity.getSupportFragmentManager(), url);
        } else {
            WebViewUtils.open(activity, url);
        }
    }
}

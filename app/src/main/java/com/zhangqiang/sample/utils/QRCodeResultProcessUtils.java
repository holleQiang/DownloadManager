package com.zhangqiang.sample.utils;

import androidx.appcompat.app.AppCompatActivity;

import com.zhangqiang.sample.ui.dialog.TaskCreateByLinkDialog;

import java.util.regex.Pattern;

public class QRCodeResultProcessUtils {

    public static void processHttpUrl(AppCompatActivity activity, String url) {
        if (Pattern.compile("\\.apk").matcher(url).find()) {
            TaskCreateByLinkDialog.newInstance(url).show(activity.getSupportFragmentManager(), "task_create_dialog");
        } else {
            WebViewUtils.open(activity, url);
        }
    }
}

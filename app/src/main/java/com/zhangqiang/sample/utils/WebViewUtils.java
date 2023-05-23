package com.zhangqiang.sample.utils;

import android.content.Context;
import android.content.Intent;

import com.zhangqiang.web.activity.WebViewActivity;

public class WebViewUtils {

    public static final String INTENT_KEY_URL = "url";

    public static void open(Context context, String url){
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra(INTENT_KEY_URL,url);
        context.startActivity(intent);
    }
}

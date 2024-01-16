package com.zhangqiang.downloadmanager.plugin.m3u8.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class Utils {

    public static String buildResourceUrl(String baseUrl, String uri) {
        Uri itemUri = Uri.parse(uri);
        if (!TextUtils.isEmpty(itemUri.getScheme())) {
            return uri;
        }
        try {
            return new URL(new URL(baseUrl), uri).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return uri;
    }
}

package com.zhangqiang.web.utils;

import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class URLUtils {

    public static String encodeUrl(String url) {
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeUrl(String encodeUrl) {
        try {
            return URLDecoder.decode(encodeUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isHttpUrl(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        return "http".equals(scheme) || "https".equals(scheme);
    }
}

package com.zhangqiang.downloadmanager.utils;

import android.net.Uri;
import android.text.TextUtils;

import java.util.regex.Pattern;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-08
 */
public class URLUtils {

    public static String getFileName(String url) {
        Uri uri = Uri.parse(url);
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null || TextUtils.isEmpty(lastPathSegment)) {
            return null;
        }
        Pattern pattern = Pattern.compile("[^/]+\\..+");
        if (pattern.matcher(lastPathSegment).matches()) {
            return lastPathSegment;
        }
        return null;
    }
}

package com.zhangqiang.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    public static void copy(Context context,String text){
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        ClipData clipData = ClipData.newPlainText(text, text);
        clipboardManager.setPrimaryClip(clipData);
    }
}

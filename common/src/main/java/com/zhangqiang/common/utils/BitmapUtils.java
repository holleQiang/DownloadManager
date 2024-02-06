package com.zhangqiang.common.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }

    public static Bitmap byteArrayToBitmap(byte[] byteArray) {
        if (byteArray == null || byteArray.length <= 0) {
            return null;
        }
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}

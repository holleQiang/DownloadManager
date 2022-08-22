package com.zhangqiang.sample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    public static Bitmap decodeBitmap(String filePath, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if(maxWidth > 0 && maxHeight > 0){
            int withRatio = (int) Math.ceil((float) options.outWidth / maxWidth);
            withRatio = Math.min(1, withRatio);
            int heightRatio = (int) Math.ceil((float) options.outHeight / maxHeight);
            heightRatio = Math.min(1, heightRatio);
            options.inSampleSize = Math.max(withRatio, heightRatio);
        }
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}

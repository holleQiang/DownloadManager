package com.zhangqiang.sample.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {

    public static DecodeResult decodeBitmap(String filePath, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int sampleSize = 1;
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        if(maxWidth < outWidth || maxHeight < outHeight){
            int withRatio = (int) Math.ceil((float) outWidth / maxWidth);
            withRatio = Math.max(1, withRatio);
            int heightRatio = (int) Math.ceil((float) outHeight / maxHeight);
            heightRatio = Math.max(1, heightRatio);
            sampleSize = Math.max(withRatio, heightRatio);
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        return new DecodeResult(BitmapFactory.decodeFile(filePath, options),sampleSize);
    }

    public static class DecodeResult{
        private final Bitmap bitmap;
        private final int sampleSize;

        public DecodeResult(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.sampleSize = sampleSize;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public int getSampleSize() {
            return sampleSize;
        }
    }

    public static Bitmap decodeBitmap(String filePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}

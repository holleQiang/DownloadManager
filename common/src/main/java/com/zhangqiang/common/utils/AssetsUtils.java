package com.zhangqiang.common.utils;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetsUtils {

    public static String readAsString(Context context,String path){
        InputStream inputStream = null;
        try {
             inputStream = context.getAssets().open(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return new InputStreamReader(inputStream).toString();
    }
}

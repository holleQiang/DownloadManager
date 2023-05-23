package com.zhangqiang.sample.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MimeTypeUtils {

    public static Map<String,String> getMimeTypes(Context context){
        InputStream is = null;
        try {
             is = context.getResources().getAssets().open("mimeTypes.json");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null){
                sb.append(line);
                line = bufferedReader.readLine();
            }
            HashMap<String,String> map = new HashMap<>();
            JSONObject jsonObject = new JSONObject(sb.toString());
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()){
                String next = keys.next();
                map.put(next,jsonObject.getString(next));
            }
            return map;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}

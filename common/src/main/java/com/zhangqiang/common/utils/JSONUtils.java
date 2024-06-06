package com.zhangqiang.common.utils;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;

public class JSONUtils {

   private static final Gson gson = new Gson();

    public static <T> T toObject(String jsonString,Class<T> tClass){
       return gson.fromJson(jsonString,tClass);
    }

    public static String toJSONString(Object object){
        return gson.toJson(object);
    }

    public static String toJSONString(InputStream object){
        return gson.newJsonReader(new InputStreamReader(object)).toString();
    }
}

package com.zhangqiang.downloadmanager.task.http;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.task.http.range.RangePart;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HttpUtils {

    public static void setRangeParams(FiledSetter setter, long start) {
        setter.setField("Range", "bytes=" + start + "-");

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }

    public static void setRangeParams(FiledSetter setter, long start, long end) {
        setter.setField("Range", "bytes=" + start + "-" + end);

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }


    public static String parseFileName(FieldGetter fieldGetter) {

        String contentDisposition = fieldGetter.getField("Content-Disposition");
        if (!TextUtils.isEmpty(contentDisposition)) {
            String[] split = contentDisposition.split(";");
            for (String item : split) {
                int index = item.indexOf("filename=");
                if (index != -1) {
                    String fileName = item.substring(index + 9).replaceAll("\"", "");
                    try {
                        fileName = URLDecoder.decode(fileName, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    return fileName;
                }
            }
        }
        return null;
    }

    public static String parseETag(FieldGetter fieldGetter) {
        String value = fieldGetter.getField("ETag");
        if (!TextUtils.isEmpty(value)) {
            return value.replaceAll("\"", "");
        }
        return null;
    }

    public static String parseLastModified(FieldGetter fieldGetter) {
        String value = fieldGetter.getField("Last-Modified");
        if (!TextUtils.isEmpty(value)) {
            return value;
        }
        return null;
    }

    public static RangePart parseRangePart(FieldGetter fieldGetter) {
        String headerField = fieldGetter.getField("Content-Range");
        if (TextUtils.isEmpty(headerField)) {
            return null;
        }
        String[] split = headerField.split(" ");
        if (split.length != 2) {
            return null;
        }
        RangePart rangePart = new RangePart();
        rangePart.setUnit(split[0]);
        String[] infoSplit = split[1].split("/");
        rangePart.setTotal(Long.parseLong(infoSplit[1]));
        String rangeStr = infoSplit[0];
        String[] rangeSplit = rangeStr.split("-");
        rangePart.setStart(Long.parseLong(rangeSplit[0]));
        rangePart.setEnd(Long.parseLong(rangeSplit[1]));
        return rangePart;
    }

}

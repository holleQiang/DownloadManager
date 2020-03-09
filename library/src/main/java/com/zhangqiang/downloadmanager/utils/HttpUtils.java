package com.zhangqiang.downloadmanager.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class HttpUtils {

    public interface RequestPropertySetter {

        void setRequestProperty(String key, String value);
    }

    public interface HeaderFieldOwner {

        String getHeaderField(String key);
    }

    public static void setRangeParams(RequestPropertySetter setter, long start) {
        setter.setRequestProperty("Range", "bytes=" + start + "-");

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }

    public static void setRangeParams(RequestPropertySetter setter, long start, long end) {
        setter.setRequestProperty("Range", "bytes=" + start + "-" + end);

//        String eTag = getETag();
//        if (!TextUtils.isEmpty(eTag)) {
//            connection.setRequestProperty("If-Range",eTag);
//        }
//        String lastModified = getLastModified();
//        if (!TextUtils.isEmpty(lastModified)) {
//            connection.setRequestProperty("If-Modified-Since",lastModified);
//        }
    }


    public static String parseFileName(HeaderFieldOwner headerFieldOwner) {

        String contentDisposition = headerFieldOwner.getHeaderField("Content-Disposition");
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

    public static String parseETag(HeaderFieldOwner headerFieldOwner) {
        String value = headerFieldOwner.getHeaderField("ETag");
        if (!TextUtils.isEmpty(value)) {
            return value.replaceAll("\"", "");
        }
        return null;
    }

    public static String parseLastModified(HeaderFieldOwner headerFieldOwner) {
        String value = headerFieldOwner.getHeaderField("Last-Modified");
        if (!TextUtils.isEmpty(value)) {
            return value;
        }
        return null;
    }

    public static RangePart parseRangePart(HeaderFieldOwner headerFieldOwner) {
        String headerField = headerFieldOwner.getHeaderField("Content-Range");
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
        rangePart.setTotal(Long.valueOf(infoSplit[1]));
        String rangeStr = infoSplit[0];
        String[] rangeSplit = rangeStr.split("-");
        rangePart.setStart(Long.valueOf(rangeSplit[0]));
        rangePart.setEnd(Long.valueOf(rangeSplit[1]));
        return rangePart;
    }

    public static class RangePart {

        private String unit;
        private long start;
        private long end;
        private long total;

        public String getUnit() {
            return unit;
        }

        public RangePart setUnit(String unit) {
            this.unit = unit;
            return this;
        }

        public long getStart() {
            return start;
        }

        public RangePart setStart(long start) {
            this.start = start;
            return this;
        }

        public long getEnd() {
            return end;
        }

        public RangePart setEnd(long end) {
            this.end = end;
            return this;
        }

        public long getTotal() {
            return total;
        }

        public RangePart setTotal(long total) {
            this.total = total;
            return this;
        }

        @Override
        public String toString() {
            return "RangePart{" +
                    "unit='" + unit + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    ", total=" + total +
                    '}';
        }
    }
}

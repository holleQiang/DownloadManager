package com.zhangqiang.downloadmanager.utils;

import java.text.DecimalFormat;

public class StringUtils {

    private static final int KB = 1024;
    private static final int MB = 1024 * 1024;
    private static final int GB = 1024 * 1024 * 1024;
    private static final DecimalFormat B_FORMAT = new DecimalFormat("0B");
    private static final DecimalFormat KB_FORMAT = new DecimalFormat("0.0KB");
    private static final DecimalFormat MB_FORMAT = new DecimalFormat("0.00MB");
    private static final DecimalFormat GB_FORMAT = new DecimalFormat("0.00GB");

    public static String formatFileLength(long length) {

        if (length < 0) {
            return "-1";
        } else if (length < KB) {
            return B_FORMAT.format(length);
        } else if (length < MB) {
            return KB_FORMAT.format((float) length / KB);
        } else if (length < GB) {
            return MB_FORMAT.format((float) length / MB);
        } else {
            return GB_FORMAT.format((float) length / GB);
        }
    }
}

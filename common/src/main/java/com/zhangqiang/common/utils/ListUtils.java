package com.zhangqiang.common.utils;

import java.util.List;

public class ListUtils {

    public static String join(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String t : list) {
            sb.append(t);
            sb.append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}

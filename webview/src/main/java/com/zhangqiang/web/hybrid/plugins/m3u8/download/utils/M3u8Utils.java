package com.zhangqiang.web.hybrid.plugins.m3u8.download.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M3u8Utils {
    private static final Pattern timePattern = Pattern.compile("time=(\\S*)");

    public static long getTime(String log) {
        Matcher matcher = timePattern.matcher(log);
        if (matcher.find() && matcher.groupCount() >= 1) {
            String timeStr = matcher.group(1);
            return parseTime(timeStr);
        }
        return -1;
    }

    /**
     * pase time form 00:01:07.36 to long
     *
     * @param time format like  00:01:07.36
     * @return long timestamp
     */
    public static long parseTime(String time) {
        try {
            String[] split = time.split("\\.");
            String part1 = split[0];
            String part2 = split[1];
            String[] items = part1.split(":");
            long hour = Long.parseLong(items[0]);
            long minutes = Long.parseLong(items[1]);
            long second = Long.parseLong(items[2]);
            long millionSecond = Long.parseLong(part2) * 10;
            return millionSecond + second * 1000 + minutes * 60 * 1000 + hour * 60 * 60 * 1000;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }
}

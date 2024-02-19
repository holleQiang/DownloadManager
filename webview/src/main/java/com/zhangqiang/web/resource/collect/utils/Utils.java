package com.zhangqiang.web.resource.collect.utils;

import java.util.regex.Pattern;

public class Utils {

    private static final String IMAGE_PATTERN = ".*\\.((png)|(jpg)|(jpeg)|(webp)|(gif)).*";
    private static final String VIDEO_PATTERN = ".*\\.((m3u8)|(mp4)|(flv)|(mkv)|(avi)).*";
    private static final String AUDIO_PATTERN = ".*\\.(mp3).*";
    private static final String CSS_PATTERN = ".*\\.(css).*";


    private static boolean matchPattern(String input,String pattern){
        return Pattern.compile(pattern).matcher(input).matches();
    }

    public static boolean isImageUrl(String input){
        return matchPattern(input,IMAGE_PATTERN);
    }

    public static boolean isVideoUrl(String input){
        return matchPattern(input,VIDEO_PATTERN);
    }

    public static boolean isAudioUrl(String input){
        return matchPattern(input,AUDIO_PATTERN);
    }

    public static boolean isCSSUrl(String input){
        return matchPattern(input,CSS_PATTERN);
    }
}

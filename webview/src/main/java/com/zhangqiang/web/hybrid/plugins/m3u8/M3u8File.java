package com.zhangqiang.web.hybrid.plugins.m3u8;

import java.util.List;

public class M3u8File {

    public int version;
    private final int mediaSequence;
    private final int targetDuration;
    private final String playListType;
    private final List<InfoItem> infoItems;

    public M3u8File(int version, int mediaSequence, int targetDuration, String playListType, List<InfoItem> infoItems) {
        this.version = version;
        this.mediaSequence = mediaSequence;
        this.targetDuration = targetDuration;
        this.playListType = playListType;
        this.infoItems = infoItems;
    }

    public int getVersion() {
        return version;
    }

    public int getMediaSequence() {
        return mediaSequence;
    }

    public int getTargetDuration() {
        return targetDuration;
    }

    public String getPlayListType() {
        return playListType;
    }

    public List<InfoItem> getInfoItems() {
        return infoItems;
    }
}

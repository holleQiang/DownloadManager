package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

import java.util.List;

public class M3u8File {

    private final int version;
    private final int mediaSequence;
    private final int targetDuration;
    private final String playListType;
    private final List<TSInfo> infoList;
    private final List<StreamInfo> streamInfoList;

    public M3u8File(int version, int mediaSequence, int targetDuration, String playListType, List<TSInfo> infoItems, List<StreamInfo> streamInfoList) {
        this.version = version;
        this.mediaSequence = mediaSequence;
        this.targetDuration = targetDuration;
        this.playListType = playListType;
        this.infoList = infoItems;
        this.streamInfoList = streamInfoList;
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

    public List<TSInfo> getInfoList() {
        return infoList;
    }

    public List<StreamInfo> getStreamInfoList() {
        return streamInfoList;
    }

    @Override
    public String toString() {
        return "M3u8File{" +
                "version=" + version +
                ", mediaSequence=" + mediaSequence +
                ", targetDuration=" + targetDuration +
                ", playListType='" + playListType + '\'' +
                ", infoList=" + infoList +
                ", streamInfoList=" + streamInfoList +
                '}';
    }
}

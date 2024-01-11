package com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean;

public class StreamInfo {
    private final int programId;
    private final long bandWidth;
    private final Resolution resolution;
    private  String uri;

    public StreamInfo(int programId, long bandWidth, Resolution resolution) {
        this.programId = programId;
        this.bandWidth = bandWidth;
        this.resolution = resolution;
    }

    public int getProgramId() {
        return programId;
    }

    public long getBandWidth() {
        return bandWidth;
    }

    public Resolution getResolution() {
        return resolution;
    }

    public String getUri() {
        return uri;
    }

    public StreamInfo setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @Override
    public String toString() {
        return "StreamInfo{" +
                "programId=" + programId +
                ", bandWidth=" + bandWidth +
                ", resolution=" + resolution +
                ", uri='" + uri + '\'' +
                '}';
    }
}

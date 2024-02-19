package com.zhangqiang.downloadmanager.plugin.m3u8.parser;

import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.KeyInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.Resolution;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.StreamInfo;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class M3u8FileParser {
    public static final String TAG = "M3u8FileParser";

    public M3u8File parse(InputStream inputStream) {

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = bufferedReader.readLine();
            if (!"#EXTM3U".equals(line)) {
                return null;
            }
            int version = 0;
            int mediaSequence = 0;
            int targetDuration = 0;
            String playListType = null;
            List<TSInfo> infoItems = new ArrayList<>();
            List<StreamInfo> streamInfoList = new ArrayList<>();
            StreamInfo currentStreamInfo = null;
            KeyInfo keyInfo = null;
            float tsDuration = 0;
            String tsUri = null;
            while (line != null) {
                if (line.startsWith("#EXT-X-VERSION")) {
                    version = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("#EXT-X-MEDIA-SEQUENCE")) {
                    mediaSequence = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("#EXT-X-TARGETDURATION")) {
                    targetDuration = Integer.parseInt(line.split(":")[1]);
                } else if (line.startsWith("#EXT-X-PLAYLIST-TYPE")) {
                    playListType = line.split(":")[1];
                } else if (line.startsWith("#EXTINF")) {
                    if (tsDuration != 0) {
                        infoItems.add(new TSInfo(tsDuration, tsUri));
                    }
                    tsDuration = Float.parseFloat(line.split(":")[1].split(",")[0]);
                } else if (line.equals("#EXT-X-ENDLIST")) {
                    if (tsDuration != 0) {
                        infoItems.add(new TSInfo(tsDuration, tsUri));
                        tsDuration = 0;
                    }
                } else if (tsDuration != 0) {
                    tsUri = line;
                } else if (line.startsWith("#EXT-X-STREAM-INF")) {
                    if (currentStreamInfo != null) {
                        streamInfoList.add(currentStreamInfo);
                    }
                    int programId = 0;
                    long bandWidth = 0;
                    Resolution resolution = null;
                    String body = line.split(":")[1];
                    String[] items = body.split(",");
                    for (String item : items) {
                        String[] pair = item.split("=");
                        String key = pair[0];
                        String value = pair[1];
                        if ("PROGRAM-ID".equals(key)) {
                            programId = Integer.parseInt(value);
                        } else if ("BANDWIDTH".equals(key)) {
                            bandWidth = Integer.parseInt(value);
                        } else if ("RESOLUTION".equals(key)) {
                            resolution = new Resolution();
                            String[] xes = value.split("x");
                            resolution.setWidth(Integer.parseInt(xes[0]));
                            resolution.setHeight(Integer.parseInt(xes[1]));
                        }
                    }
                    currentStreamInfo = new StreamInfo(programId, bandWidth, resolution);
                } else if (currentStreamInfo != null) {
                    currentStreamInfo.setUri(line);
                } else if (line.startsWith("#EXT-X-KEY")) {
                    String body = line.substring(11);
                    String[] bodyItems = body.split(",");
                    String methodName = null;
                    String uri = null;
                    String iv = null;
                    for (String bodyItem : bodyItems) {
                        String[] split = bodyItem.split("=");
                        String key = split[0];
                        String value = split[1];
                        if ("METHOD".equals(key)) {
                            methodName = value;
                        } else if ("URI".equals(key)) {
                            uri = value;
                        } else if ("IV".equals(key)) {
                            iv = value;
                        }
                    }
                    keyInfo = new KeyInfo(methodName, uri, iv);
                }
                LogUtils.i(TAG, "============" + line);
                line = bufferedReader.readLine();
            }
            if (tsDuration != 0) {
                infoItems.add(new TSInfo(tsDuration, tsUri));
            }
            if (currentStreamInfo != null) {
                streamInfoList.add(currentStreamInfo);
            }
            return new M3u8File(version, mediaSequence, targetDuration, playListType, infoItems, streamInfoList, keyInfo);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

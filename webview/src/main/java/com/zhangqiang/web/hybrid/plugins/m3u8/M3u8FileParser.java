package com.zhangqiang.web.hybrid.plugins.m3u8;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class M3u8FileParser {

    private final String filePath;

    public M3u8FileParser(String filePath) {
        this.filePath = filePath;
    }

    public M3u8File parse() {

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line = bufferedReader.readLine();
            if (!"#EXTM3U".equals(line)) {
                return null;
            }
            int version = 0;
            int mediaSequence = 0;
            int targetDuration = 0;
            String playListType = null;
            List<InfoItem> infoItems = new ArrayList<>();
            InfoItem currentItem = null;
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
                    if (currentItem != null) {
                        infoItems.add(currentItem);
                    }
                    currentItem = new InfoItem();
                    currentItem.setDuration(Float.parseFloat(line.split(":")[1].split(",")[0]));
                } else if (line.equals("#EXT-X-ENDLIST")) {
                    if (currentItem != null) {
                        infoItems.add(currentItem);
                        currentItem = null;
                    }
                } else if (currentItem != null) {
                    currentItem.setUri(line);
                }
                line = bufferedReader.readLine();
            }
            return new M3u8File(version, mediaSequence, targetDuration, playListType, infoItems);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

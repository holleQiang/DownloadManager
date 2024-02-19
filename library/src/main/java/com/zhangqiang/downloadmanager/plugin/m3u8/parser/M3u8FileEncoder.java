package com.zhangqiang.downloadmanager.plugin.m3u8.parser;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.KeyInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Locale;

public class M3u8FileEncoder {

    public void encode(M3u8File m3u8File, OutputStream outputStream) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
        bufferedWriter.write("#EXTM3U\n");
        bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-VERSION:%d\n", m3u8File.getVersion()));
        bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-TARGETDURATION:%d\n", m3u8File.getTargetDuration()));
        bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-TARGETDURATION:%d\n", m3u8File.getTargetDuration()));
        bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-PLAYLIST-TYPE:%s\n", m3u8File.getPlayListType()));
        bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-MEDIA-SEQUENCE:%d\n", m3u8File.getMediaSequence()));
        KeyInfo keyInfo = m3u8File.getKeyInfo();
        if (keyInfo != null) {
            String iv = keyInfo.getIV();
            if (TextUtils.isEmpty(iv)) {
                bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-KEY:METHOD=%s,URI=%s\n", keyInfo.getMethod(), keyInfo.getUri()));
            } else {
                bufferedWriter.write(String.format(Locale.ENGLISH, "#EXT-X-KEY:METHOD=%s,URI=%s,IV=%s\n", keyInfo.getMethod(), keyInfo.getUri(), keyInfo.getIV()));
            }
        }
        List<TSInfo> infoList = m3u8File.getInfoList();
        if (infoList != null) {
            for (TSInfo tsInfo : infoList) {
                bufferedWriter.write(String.format(Locale.ENGLISH, "#EXTINF:%f,\n", tsInfo.getDuration()));
                bufferedWriter.write(tsInfo.getUri() + "\n");
            }
        }
        bufferedWriter.write("#EXT-X-ENDLIST\n");
        bufferedWriter.flush();
        bufferedWriter.close();
    }
}

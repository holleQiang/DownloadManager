package com.zhangqiang.downloadmanager.utils;

import com.zhangqiang.downloadmanager.plugin.http.part.PartInfo;
import com.zhangqiang.downloadmanager.plugin.http.range.RangePart;

import java.util.ArrayList;
import java.util.List;

public class RangePartUtils {

    public static List<PartInfo> toPartInfoList(RangePart rangePart,int count){
        List<PartInfo> partInfoList = new ArrayList<>();
        final long total = rangePart.getTotal();
        long eachDownload = total / count;
        long resetDownload = total % count;
        for (int i = 0; i < count; i++) {
            final long start = i * eachDownload;
            long end = start + eachDownload;
            if (i == count - 1) {
                end += resetDownload - 1;
            }
            partInfoList.add(new PartInfo(start, end));
        }
        return partInfoList;
    }
}

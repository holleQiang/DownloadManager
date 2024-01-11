package com.zhangqiang.sample.utils;

import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.ftp.request.FtpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.m3u8.request.M3u8DownloadRequest;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.sample.manager.SettingsManager;

import java.io.File;

public class DownloadUtils {

    public static DownloadTask downloadHttpUrl(String httpUrl) {
        return downloadHttpUrl(httpUrl,null);
    }

    public static DownloadTask downloadHttpUrl(String httpUrl,String targetFileName) {
        File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
        DownloadRequest downloadRequest = new HttpDownloadRequest(dirFile.getAbsolutePath(),
                targetFileName,
                httpUrl,
                SettingsManager.getInstance().getHttpDownloadThreadSize().get());
        return DownloadManager.getInstance().enqueue(downloadRequest);
    }

    public static void downloadFtpUrl(String ftpUrl) {
        Uri uri = Uri.parse(ftpUrl);
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (!TextUtils.isEmpty(userInfo)) {
            String[] split = userInfo.split(":");
            if (split.length >= 1) {
                username = split[0];
            }
            if (split.length >= 2) {
                password = split[1];
            }
        }

        String path = uri.getPath();
        File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
        String ftpDir = path.substring(0, path.lastIndexOf("/"));
        if (TextUtils.isEmpty(ftpDir)) {
            ftpDir = "/";
        }
        DownloadRequest downloadRequest = new FtpDownloadRequest(dirFile.getAbsolutePath(),
                null,
                uri.getHost(),
                uri.getPort(),
                username,
                password,
                ftpDir,
                uri.getLastPathSegment()
        );
        DownloadManager.getInstance().enqueue(downloadRequest);
    }

    public static boolean downloadLink(String link) {
        Uri uri = Uri.parse(link);
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            String lastPathSegment = uri.getLastPathSegment();
            if(lastPathSegment != null && lastPathSegment.endsWith(".m3u8")){
                downloadM3u8Url(link,null);
            }else {
                downloadHttpUrl(link);
            }
            return true;
        } else if ("ftp".equals(scheme)) {
            downloadFtpUrl(link);
            return true;
        }
        return false;
    }

    public static DownloadTask downloadM3u8Url(String url,String targetFileName) {
        File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
        M3u8DownloadRequest downloadRequest = new M3u8DownloadRequest(dirFile.getAbsolutePath(),
                targetFileName,
                url);
        return DownloadManager.getInstance().enqueue(downloadRequest);
    }
}

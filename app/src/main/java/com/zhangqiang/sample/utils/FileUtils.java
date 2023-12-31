package com.zhangqiang.sample.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.zhangqiang.sample.manager.SettingsManager;

import java.io.File;

public class FileUtils {

    public static File getDownloadDir() {
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        File dir = new File(externalStorageDirectory, SettingsManager.getInstance().getSaveDir());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    public static void refreshMediaFile(Context context, File file) {
        Uri localUri = Uri.fromFile(file);
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
        context.sendBroadcast(localIntent);
    }
}

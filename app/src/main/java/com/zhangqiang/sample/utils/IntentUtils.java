package com.zhangqiang.sample.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

import java.io.File;

public class IntentUtils {

    public static void openFile(Context context, File file, String mimeType) {

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                uri = Uri.fromFile(file);
            }
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(uri, mimeType);
            context.startActivity(intent);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}

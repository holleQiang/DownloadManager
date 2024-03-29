package com.zhangqiang.common.utils;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zhangqiang.common.result.ActivityStarter;
import com.zhangqiang.common.result.ActivityStarterOwner;

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
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void openChooseImagePage(ActivityStarterOwner owner, ContentResolver contentResolver, ChooseImagePageCallback callback) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        owner.getActivityStarter().startActivityForResult(intent, new ActivityStarter.ActivityResultCallback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if (resultCode == RESULT_OK) {
                    String imageFilePath = getImageFilePathFromUri(contentResolver, data.getData());
                    if (!TextUtils.isEmpty(imageFilePath)) {
                        callback.onChooseImage(imageFilePath);
                    }
                }
            }
        });
    }

    public static void openActivityByUri(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public interface ChooseImagePageCallback {
        void onChooseImage(String imageFilePath);
    }

    public static String getImageFilePathFromUri(ContentResolver contentResolver, Uri uri) {
        try (Cursor query = contentResolver.query(uri, null, null, null, null)) {
            if (query.moveToFirst()) {
                return query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA));
            } else {
                return null;
            }
        }
    }
}

package com.zhangqiang.sample.utils;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.sample.base.result.ActivityStarter;
import com.zhangqiang.sample.base.result.ActivityStarterOwner;
import com.zhangqiang.sample.business.container.ContainerActivity;
import com.zhangqiang.sample.business.container.processor.QRCodeProcessor;
import com.zhangqiang.sample.ui.MainActivity;

import java.io.File;
import java.util.Map;

public class IntentUtils {

    public static void openFileOrThrow(Context context, File file, String mimeType) {

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
    }

    public static void openFile(Context context, File file, String mimeType) {

        try {
            openFileOrThrow(context, file, mimeType);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    public static void openFileSmart(Context context, File file, String mimeType) {

        String fileFormat = FileUtils.getFileFormat(file.getAbsolutePath());
        if (fileFormat != null) {
            Map<String, String> mimeTypes = MimeTypeUtils.getMimeTypes(context);
            if (mimeTypes != null) {
                String assertMimeType = mimeTypes.get(fileFormat);
                if (assertMimeType != null && !assertMimeType.equals(mimeType)) {
                    try {
                        openFileOrThrow(context, file, assertMimeType);
                        return;
                    } catch (Throwable e) {
                        //ignore
                    }
                }
            }
        }
        openFile(context, file, mimeType);
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

    public static void openMainActivity(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("link", url);
        context.startActivity(intent);
    }

    public static void openActivityByUri(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        context.startActivity(intent);
    }

    public static void openDir(Context context, String dir) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String mimeType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mimeType = DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            mimeType = "vnd.android.document/directory";
        }
        File file = new File(dir);
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", file);
        intent.setDataAndType(uri, mimeType);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public interface ChooseImagePageCallback {
        void onChooseImage(String imageFilePath);
    }

    public static void openQRCodeDecodePage(Context context, String imageFilePath) {
        Intent intent = new Intent(context, ContainerActivity.class);
        intent.putExtra(QRCodeProcessor.EXTRA_IMAGE_FILE_PATH, imageFilePath);
        context.startActivity(intent);
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

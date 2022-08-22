package com.zhangqiang.sample.utils;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import com.zhangqiang.options.Options;
import com.zhangqiang.sample.base.result.ActivityStarter;
import com.zhangqiang.sample.base.result.ActivityStarterOwner;
import com.zhangqiang.sample.business.web.WebViewActivity;
import com.zhangqiang.sample.ui.MainActivity;
import com.zhangqiang.sample.ui.decodeqrcode.QRCodeDecodeActivity;

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


    public static void openChooseImagePage(ActivityStarterOwner owner, ContentResolver contentResolver,ChooseImagePageCallback callback) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        owner.getActivityStarter().startActivityForResult(intent, new ActivityStarter.ActivityResultCallback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if(resultCode == RESULT_OK){
                    String imageFilePath = getImageFilePathFromUri(contentResolver, data.getData());
                    if(!TextUtils.isEmpty(imageFilePath)){
                        callback.onChooseImage(imageFilePath);
                    }
                }
            }
        });
    }

    public interface ChooseImagePageCallback{
        void onChooseImage(String imageFilePath);
    }

    public  static void openQRCodeDecodePage(Context context,String imageFilePath){
        Intent intent = new Intent(context, QRCodeDecodeActivity.class);
        intent.putExtra(QRCodeDecodeActivity.EXTRA_IMAGE_FILE_PATH,imageFilePath);
        context.startActivity(intent);
    }

    public static String getImageFilePathFromUri(ContentResolver contentResolver,Uri uri){
        try (Cursor query = contentResolver.query(uri, null, null, null, null)) {
            if (query.moveToNext()) {
                return query.getString(query.getColumnIndex(MediaStore.Images.Media.DATA));
            }else {
                return null;
            }
        }
    }
}

package com.zhangqiang.sample.business.container.processor;

import android.content.ClipData;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.zxing.NotFoundException;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.business.container.ContainerActivity;
import com.zhangqiang.sample.business.container.ContainerProcessor;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.QRCodeDecodeUtils;
import com.zhangqiang.sample.utils.QRCodeResultProcessUtils;
import com.zhangqiang.sample.utils.RxJavaUtils;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class QRCodeProcessor implements ContainerProcessor {

    public static final String EXTRA_IMAGE_FILE_PATH = "image_file_path";

    @Override
    public boolean processor(ContainerActivity activity) {
        String imageFilePath = null;
        Intent intent = activity.getIntent();
        if (Objects.equals(intent.getAction(), Intent.ACTION_SEND)) {
            if ("image/*".equals(intent.getType())) {
                ClipData.Item item = intent.getClipData().getItemAt(0);
                imageFilePath = IntentUtils.getImageFilePathFromUri(activity.getContentResolver(), item.getUri());
            }
        }
        if (TextUtils.isEmpty(imageFilePath)) {
            imageFilePath = intent.getStringExtra(EXTRA_IMAGE_FILE_PATH);
        }
        if (imageFilePath == null || TextUtils.isEmpty(imageFilePath)) {
            return false;
        }
        Observable.just(imageFilePath).map(new Function<String, String>() {
                    @Override
                    public String apply(String path) throws Exception {
                        return QRCodeDecodeUtils.decodeQRCode(path);
                    }
                })
                .compose(RxJavaUtils.bindLifecycle(activity))
                .compose(RxJavaUtils.applyIOMainSchedules())
                .compose(RxJavaUtils.withLoadingDialog(activity))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        if (TextUtils.isEmpty(s)) {
                            Toast.makeText(activity, activity.getString(R.string.qr_code_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            QRCodeResultProcessUtils.processHttpUrl(activity, s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if (e instanceof NotFoundException) {
                            Toast.makeText(activity, activity.getString(R.string.qr_code_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, activity.getString(R.string.qr_code_decode_fail), Toast.LENGTH_SHORT).show();
                        }
                        activity.finish();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        activity.finish();
                    }
                });
        return true;
    }
}

package com.zhangqiang.sample.ui.decodeqrcode;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.zxing.NotFoundException;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.utils.QRCodeDecodeUtils;
import com.zhangqiang.sample.utils.QRCodeResultProcessUtils;
import com.zhangqiang.sample.utils.RxJavaUtils;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;

public class QRCodeDecodeActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_FILE_PATH = "image_file_path";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String imageFilePath = getIntent().getStringExtra(EXTRA_IMAGE_FILE_PATH);
        if(TextUtils.isEmpty(imageFilePath)){
            finish();
            return;
        }
        Observable.create((ObservableOnSubscribe<String>) e -> {
            e.onNext(QRCodeDecodeUtils.decodeQRCode(imageFilePath));
            e.onComplete();
        })
                .compose(RxJavaUtils.bindLifecycle(QRCodeDecodeActivity.this))
                .compose(RxJavaUtils.applyIOMainSchedules())
                .compose(RxJavaUtils.withLoadingDialog(QRCodeDecodeActivity.this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        if(TextUtils.isEmpty(s)){
                            Toast.makeText(QRCodeDecodeActivity.this,getString(R.string.qr_code_not_found),Toast.LENGTH_SHORT).show();
                        }else {
                            QRCodeResultProcessUtils.processHttpUrl(QRCodeDecodeActivity.this,s);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        if(e instanceof NotFoundException){
                            Toast.makeText(QRCodeDecodeActivity.this,getString(R.string.qr_code_not_found),Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(QRCodeDecodeActivity.this,getString(R.string.qr_code_decode_fail),Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        finish();
                    }
                });
    }
}

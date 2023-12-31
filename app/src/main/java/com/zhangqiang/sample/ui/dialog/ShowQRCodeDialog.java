package com.zhangqiang.sample.ui.dialog;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.base.permission.PermissionHelper;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.BitmapUtils;
import com.zhangqiang.sample.utils.FileUtils;
import com.zhangqiang.sample.utils.PermissionUtils;
import com.zhangqiang.sample.utils.QRCodeEncodeUtils;
import com.zhangqiang.sample.utils.RxJavaUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class ShowQRCodeDialog extends BaseDialogFragment {

    private String text;

    public static ShowQRCodeDialog newInstance(String text) {
        Bundle bundle = new Bundle();
        bundle.putString("text", text);
        ShowQRCodeDialog dialog = new ShowQRCodeDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            text = arguments.getString("text");
        }
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

            }
        });
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_show_qrcode;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
        if (TextUtils.isEmpty(text)) {
            return;
        }
        view.findViewById(R.id.bt_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getPermissionHelper().requestPermissionsObservable(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE, 1000)
                        .map(PermissionHelper.applyPermissionGrant())
                        .map(new Function<Boolean, Boolean>() {
                            @Override
                            public Boolean apply(Boolean aBoolean) throws Exception {
                                if (!aBoolean) {
                                    return false;
                                }
                                ImageView imageView = view.findViewById(R.id.iv_image);
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                String fileName = MD5Utils.getMD5(text) + ".jpg";
                                File file = new File(FileUtils.getDownloadDir(), fileName);
                                boolean success = BitmapUtils.saveBitmap(file.getAbsolutePath(), bitmap);
                                if (success) {
                                    FileUtils.refreshMediaFile(view.getContext(), file);
                                }
                                return success;
                            }
                        })
                        .compose(RxJavaUtils.bindLifecycle(ShowQRCodeDialog.this))
                        .subscribe(new BaseObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                Toast.makeText(getActivity(), aBoolean ? getString(R.string.save_success) : getString(R.string.save_fail), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        Observable.just(text)
                .map(new Function<String, Bitmap>() {
                    @Override
                    public Bitmap apply(String s) throws Exception {
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int size = displayMetrics.widthPixels;
                        return QRCodeEncodeUtils.createQRCodeBitmap(s, size, size);
                    }
                })
                .compose(RxJavaUtils.applyIOMainSchedules())
                .compose(RxJavaUtils.bindLifecycle(this))
                .subscribe(new BaseObserver<Bitmap>() {
                    @Override
                    public void onNext(Bitmap bitmap) {
                        ImageView imageView = view.findViewById(R.id.iv_image);
                        imageView.setImageBitmap(bitmap);
                    }
                });
    }
}

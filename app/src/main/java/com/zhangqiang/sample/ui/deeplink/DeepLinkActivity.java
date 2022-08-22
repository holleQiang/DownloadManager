package com.zhangqiang.sample.ui.deeplink;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.utils.IntentUtils;

import java.util.Objects;

public class DeepLinkActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (Objects.equals(intent.getAction(), Intent.ACTION_SEND)) {
            if ("image/*".equals(intent.getType())) {
                ClipData.Item item = intent.getClipData().getItemAt(0);
                String imageFilePath = IntentUtils.getImageFilePathFromUri(getContentResolver(), item.getUri());
                if(!TextUtils.isEmpty(imageFilePath)){
                    IntentUtils.openQRCodeDecodePage(this,imageFilePath);
                }
            }
        }
        finish();
    }
}

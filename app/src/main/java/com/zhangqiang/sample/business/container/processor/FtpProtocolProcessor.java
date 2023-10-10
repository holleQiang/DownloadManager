package com.zhangqiang.sample.business.container.processor;

import android.net.Uri;

import com.zhangqiang.sample.business.container.ContainerActivity;
import com.zhangqiang.sample.business.container.ContainerProcessor;
import com.zhangqiang.sample.utils.IntentUtils;

public class FtpProtocolProcessor implements ContainerProcessor {
    @Override
    public boolean processor(ContainerActivity activity) {
        Uri data = activity.getIntent().getData();
        if (data == null) {
            return false;
        }
        if ("ftp".equals(data.getScheme())) {
            IntentUtils.openMainActivity(activity, data.toString());
            return true;
        }
        return false;
    }
}

package com.zhangqiang.sample.business.container.processor;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.business.container.ContainerActivity;
import com.zhangqiang.sample.business.container.ContainerProcessor;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.web.manager.WebManager;

import java.util.Arrays;
import java.util.HashSet;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpProcessor implements ContainerProcessor {

    @Override
    public boolean processor(ContainerActivity activity) {
        Uri data = activity.getIntent().getData();
        if (data != null) {
            if ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) {
                String url = data.toString();
                Observable.just(url).map(new Function<String, CheckResult>() {
                            @Override
                            public CheckResult apply(String url) throws Exception {

                                Response response = new OkHttpClient().newCall(new Request.Builder()
                                                .url(url)
                                                .get()
                                                .build())
                                        .execute();
                                if (response.isSuccessful()) {
                                    return new CheckResult(url, response.header("content-type"));
                                } else {
                                    throw new RuntimeException("http error,code:"
                                            + response.code()
                                            + "message:" + response.message());
                                }
                            }
                        })
                        .compose(RXJavaUtils.applyIOMainSchedules())
                        .compose(RXJavaUtils.withLoadingDialog(activity))
                        .compose(RXJavaUtils.bindLifecycle(activity))
                        .subscribe(new BaseObserver<CheckResult>() {
                            @Override
                            public void onNext(CheckResult result) {
                                if (result.contentTypeContains("text/html")) {
                                    WebManager.getInstance().openWebViewActivity(activity,result.getUrl());
                                }else {
                                    IntentUtils.openMainActivity(activity,url);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                super.onError(e);
                                showCheckInternetToast(activity);
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
        return false;
    }

    private static void showCheckInternetToast(ContainerActivity activity) {
        Toast.makeText(activity, activity.getString(R.string.please_check_your_internet), Toast.LENGTH_SHORT).show();
    }

    private static class CheckResult {
        private final String url;
        private final String contentType;
        private final HashSet<String> contentTypeItemSet = new HashSet<>();

        public CheckResult(String url, String contentType) {
            this.url = url;
            this.contentType = contentType;
            if (!TextUtils.isEmpty(contentType)) {
                String[] items = contentType.split(";");
                contentTypeItemSet.addAll(Arrays.asList(items));
            }
        }

        public boolean contentTypeContains(String item) {
            return contentTypeItemSet.contains(item);
        }

        public String getUrl() {
            return url;
        }

        public String getContentType() {
            return contentType;
        }
    }
}

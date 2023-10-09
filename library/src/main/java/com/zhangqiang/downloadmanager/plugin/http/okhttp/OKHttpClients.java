package com.zhangqiang.downloadmanager.plugin.http.okhttp;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.webkit.WebSettings;

import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-02
 */
public class OKHttpClients {

    private static volatile OkHttpClient okHttpClient;

    public static OkHttpClient getDefault(final Context context) {

        if (okHttpClient == null) {
            synchronized (OKHttpUtils.class) {
                if (okHttpClient == null) {
                    okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(5, TimeUnit.SECONDS)
                            .addInterceptor(new Interceptor() {
                                @Override
                                public Response intercept(@NonNull Chain chain) throws IOException {
                                    Request request = chain.request().newBuilder()
                                            .removeHeader("User-Agent")
                                            .addHeader("Connection", "keep-alive")
                                            .addHeader("User-Agent", getUserAgent(context))
                                            .build();
                                    return chain.proceed(request);
                                }
                            })
                            .retryOnConnectionFailure(true)
                            .build();
                }
            }
        }
        return okHttpClient;
    }

    private static String getUserAgent(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return WebSettings.getDefaultUserAgent(context);
        } else {
            return "Mozilla/5.0 (Linux; Android 8.1.0; Android SDK built for x86 Build/OSM1.180201.031; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/69.0.3497.100 Mobile Safari/537.36";
        }
    }
}

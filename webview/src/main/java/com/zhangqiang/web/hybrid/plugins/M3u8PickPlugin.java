package com.zhangqiang.web.hybrid.plugins;

import android.net.Uri;
import android.webkit.WebView;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnLoadResourceListener;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

public class M3u8PickPlugin implements WebPlugin {

    public interface Callback {

        void onReceiveM3u8Resource(String url);
    }



    private final Callback callback;

    public M3u8PickPlugin(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnLoadResourceListener(new OnLoadResourceListener() {
                    @Override
                    public void onLoadResource(WebView view, String url) {
                        Uri uri = Uri.parse(url);
                        String lastPathSegment = uri.getLastPathSegment();
                        if (lastPathSegment != null && lastPathSegment.endsWith(".m3u8")) {
                            callback.onReceiveM3u8Resource(url);
                        }
                    }
                });
            }
        });
    }
}

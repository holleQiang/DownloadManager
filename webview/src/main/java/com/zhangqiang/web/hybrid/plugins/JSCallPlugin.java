package com.zhangqiang.web.hybrid.plugins;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.hybrid.interfaces.JSCallHandler;
import com.zhangqiang.web.hybrid.interfaces.JavaScriptInterface;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

import java.util.HashMap;
import java.util.UUID;

public class JSCallPlugin implements WebPlugin {

    private WebView webView = null;
    private final HashMap<String, ValueCallback<String>> callbackHashMap = new HashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(State state, State oldState) {

                        if (state == State.WEB_VIEW_CREATE) {
                            webView = webContext.getWebView();
                            JavaScriptInterface.addToWebView(webView, javaScriptInterface);
                            javaScriptInterface.addJSCallHandler(jsCallHandler);
                        } else if (state == State.WEB_VIEW_DESTROY) {
                            javaScriptInterface.removeJSCallHandler(jsCallHandler);
                            mainHandler.removeCallbacksAndMessages(null);
                            callbackHashMap.clear();
                            if (webView != null) {
                                JavaScriptInterface.removeFromWebView(webView);
                                webView = null;
                            }
                        }
                    }
                });
            }
        });
    }


    private final JSCallHandler jsCallHandler = new JSCallHandler() {
        @Override
        public void handleJSCall(String callId, String callBody) {
            ValueCallback<String> callback = callbackHashMap.get(callId);
            callbackHashMap.remove(callId);
            if (callback != null) {
                callback.onReceiveValue(callBody);
            }
        }
    };

    private void performEvaluateJavascript(String callId, String js, ValueCallback<String> valueCallback) {
        callbackHashMap.put(callId, valueCallback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, null);
        } else {
            webView.loadUrl("javascript:" + js);
        }
        WebLogger.info("=======js====" + js);
    }

    public void evaluateJavascript(String callId, String js, ValueCallback<String> valueCallback) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    performEvaluateJavascript(callId, js, valueCallback);
                }
            });
        } else {
            performEvaluateJavascript(callId, js, valueCallback);
        }
    }

    public void invokeHybridMethod(HybridMethod hybridMethod) {
        WebLogger.info("=======invokeHybridMethod=====" + hybridMethod.getMethodName());
        String callId = UUID.randomUUID().toString();
        evaluateJavascript(callId, hybridMethod.buildJavascript(new CallbackJavascriptBuilder() {
            @Override
            public String buildCallbackJavascript(String objectName, String prefix) {
                return JavaScriptInterface.buildCallbackJavascript(callId, objectName,prefix);
            }
        }), new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                hybridMethod.dispatchCallback(value);
            }
        });
    }
}

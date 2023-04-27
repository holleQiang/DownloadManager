package com.zhangqiang.web.hybrid;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.zhangqiang.web.log.WebLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptInterface {

    public static final String METHOD_NAME = "method_name";
    public static final String METHOD_ARG = "method_arg";
    public static final String NATIVE_INTERFACE = "nativeInterface";

    private final List<HybridMethod> hybridMethods = new ArrayList<>();
    private WebView attachedWebView = null;

    public void registerHybridMethod(HybridMethod hybridMethod) {
        for (int i = 0; i < hybridMethods.size(); i++) {
            if (hybridMethods.get(i).getMethodName().equals(hybridMethod.getMethodName())) {
                return;
            }
        }
        hybridMethods.add(hybridMethod);
    }

    @SuppressLint("AddJavascriptInterface")
    public void attachToWebView(WebView webView) {
        detachFromWebView();
        webView.addJavascriptInterface(this, NATIVE_INTERFACE);
        attachedWebView = webView;
    }

    public void detachFromWebView() {
        if (attachedWebView == null) {
            return;
        }
        attachedWebView.removeJavascriptInterface(NATIVE_INTERFACE);
        attachedWebView = null;
    }

    public void call(HybridMethod hybridMethod) {
        if (attachedWebView == null) {
            throw new IllegalStateException("interface has not attach to web view");
        }
        InvokeJSBuilder builder = new InvokeJSBuilder() {
            @Override
            public String buildInvokeJS(String argDescriptor, Options options) {
                return JavaScriptInterface.buildInvokeJS(hybridMethod.getMethodName(), argDescriptor,options.getLinePrefix());
            }
        };
        String invokeJS = hybridMethod.buildInvokeJS(builder);
        WebLogger.info("load js:" + invokeJS);
        attachedWebView.loadUrl(invokeJS);
    }

    public void call(String methodName) {
        if (attachedWebView == null) {
            throw new IllegalStateException("interface has not attach to web view");
        }
        for (HybridMethod hybridMethod : hybridMethods) {
            if (hybridMethod.getMethodName().equals(methodName)) {
                call(hybridMethod);
                break;
            }
        }
    }

    @JavascriptInterface
    public void invoke(String args) {
        try {
            JSONObject jsonObject = new JSONObject(args);
            String methodName = jsonObject.optString(METHOD_NAME);
            String methodArg = jsonObject.optString(METHOD_ARG);
            if (TextUtils.isEmpty(methodName)) {
                WebLogger.error("method name cannot be empty!!");
                return;
            }
            for (int i = hybridMethods.size() - 1; i >= 0; i--) {
                HybridMethod hybridMethod = hybridMethods.get(i);
                if (methodName.equals(hybridMethod.getMethodName())) {
                    hybridMethod.dispatchJSCall(methodArg);
                    break;
                }
            }
            WebLogger.error("cannot find target method with name :" + methodName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String buildInvokeJS(String methodName, String argDescriptor, String linePrefix) {
        return linePrefix + "const argObj = {};\n"
                + linePrefix + "argObj." + METHOD_NAME + "='" + methodName + "';\n"
                + linePrefix + "argObj." + METHOD_ARG + "=" + argDescriptor + ";\n"
                + linePrefix + "window." + NATIVE_INTERFACE + ".invoke(JSON.stringify(argObj));\n";
    }

}

package com.zhangqiang.web.hybrid.interfaces;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JavaScriptInterface {

    private static final String NATIVE_INTERFACE = "nativeInterface2";
    public static final String CALL_ID = "call_id";
    public static final String CALL_BODY = "call_body";
    private final List<JSCallHandler> jsCallHandlers = new ArrayList<>();

    @JavascriptInterface
    public void invoke(String args) {
        String callId;
        String callBody;
        try {
            JSONObject jsonObject = new JSONObject(args);
            callId = jsonObject.optString(CALL_ID);
            if (TextUtils.isEmpty(callId)) {
                throw new RuntimeException("method name cannot be empty!!");
            }
            callBody = jsonObject.optString(CALL_BODY);
            if (TextUtils.isEmpty(callBody)) {
                throw new RuntimeException("method name cannot be empty!!");
            }
            for (int i = jsCallHandlers.size() - 1; i >= 0; i--) {
                jsCallHandlers.get(i).handleJSCall(callId, callBody);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("AddJavascriptInterface")
    public static void addToWebView(WebView webView, JavaScriptInterface javaScriptInterface) {
        webView.addJavascriptInterface(javaScriptInterface, NATIVE_INTERFACE);
    }

    public static void removeFromWebView(WebView webView) {
        webView.removeJavascriptInterface(NATIVE_INTERFACE);
    }

    public static String buildCallbackJavascript(String callId, String callBodyName,String prefix) {

        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append("const argObj = {};\n");
        sb.append(prefix).append("argObj." + CALL_ID + "='").append(callId).append("';\n");
        sb.append(prefix).append("argObj." + CALL_BODY + "=").append(callBodyName).append(";\n");
        sb.append(prefix).append("window.").append(NATIVE_INTERFACE).append(".invoke(JSON.stringify(argObj));\n");
        return sb.toString();
    }

    public void addJSCallHandler(JSCallHandler handler) {
        if (this.jsCallHandlers.contains(handler)) {
            return;
        }
        this.jsCallHandlers.add(handler);
    }

    public void removeJSCallHandler(JSCallHandler handler) {
        jsCallHandlers.remove(handler);
    }
}

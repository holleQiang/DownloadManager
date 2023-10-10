package com.zhangqiang.web.hybrid;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.log.WebLogger;

import org.json.JSONException;
import org.json.JSONObject;

public class JavaScriptInterface {

    public static final String METHOD_NAME = "method_name";
    public static final String METHOD_ARG = "method_arg";
    public static final String CALL_INDEX = "call_index";
    public static final String NATIVE_INTERFACE = "nativeInterface";

    private WebContext attachedWebContext = null;

    @SuppressLint("AddJavascriptInterface")
    public void attachToWebContext(WebContext webContext) {
        detachFromWebContext();
        webContext.getWebView().addJavascriptInterface(this, NATIVE_INTERFACE);
        attachedWebContext = webContext;
    }

    public void detachFromWebContext() {
        if (attachedWebContext == null) {
            return;
        }
        clearRecords();
        attachedWebContext = null;
    }

    public void call(HybridMethod hybridMethod) {
        CallContext callContext = new CallContext();
        CallbackJSBuilder builder = new CallbackJSBuilder() {

            @Override
            protected String onBuildCallbackJS(String argDescriptor, Options options) {
                CallRecord newRecord = nextRecord(hybridMethod);
                callContext.callRecord = newRecord;
                String callbackJS = JavaScriptInterface.buildCallbackJS(newRecord, argDescriptor, options.getLinePrefix());
                callContext.callbackJS = callbackJS;
                return callbackJS;
            }
        };
        String invokeJS = hybridMethod.buildInvokeJS(builder);
        if (TextUtils.isEmpty(invokeJS)) {
            WebLogger.error("invoke js cannot be empty:" + hybridMethod.getMethodName());
            return;
        }
        if (!TextUtils.isEmpty(callContext.callbackJS)) {
            addRecord(callContext.callRecord);
        }
        WebLogger.info("load js:\n" + invokeJS);
        callJS(invokeJS);
    }

    public void callJS(String js) {

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (attachedWebContext == null) {
                    throw new IllegalStateException("interface has not attach to web context");
                }
                WebView webView = attachedWebContext.getWebView();
                if (webView == null) {
                    return;
                }
                evaluateJS(webView, js);
            }
        });
    }

    private void evaluateJS(WebView webView, String js) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(js, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String value) {

                }
            });
        } else {
            webView.loadUrl("javascript:" + js);
        }
    }

    @JavascriptInterface
    public void invoke(String args) {
        String methodName;
        int callIndex;
        try {
            JSONObject jsonObject = new JSONObject(args);
            methodName = jsonObject.optString(METHOD_NAME);
            if (TextUtils.isEmpty(methodName)) {
                throw new RuntimeException("method name cannot be empty!!");
            }
            callIndex = jsonObject.optInt(CALL_INDEX, -1);
            if (callIndex == -1) {
                throw new RuntimeException("method name cannot be empty!!");
            }
            String methodArg = jsonObject.optString(METHOD_ARG);
            dispatchJSCall(methodName, callIndex, methodArg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static String buildCallbackJS(CallRecord callRecord, String argDescriptor, String linePrefix) {

        String methodName = callRecord.hybridMethod.getMethodName();
        StringBuilder sb = new StringBuilder();
        sb.append(linePrefix);
        sb.append("const argObj = {};\n");
        sb.append(linePrefix);
        sb.append("argObj." + METHOD_NAME + "='").append(methodName).append("';\n");
        sb.append(linePrefix);
        sb.append("argObj." + CALL_INDEX + "='").append(callRecord.index).append("';\n");
        if (!TextUtils.isEmpty(argDescriptor)) {
            sb.append(linePrefix);
            sb.append("argObj." + METHOD_ARG + "=").append(argDescriptor).append(";\n");
        }
        sb.append(linePrefix);
        sb.append("window." + NATIVE_INTERFACE + ".invoke(JSON.stringify(argObj));\n");
        return sb.toString();
    }


    static class CallRecord {
        HybridMethod hybridMethod;
        int index;
        CallRecord next;
    }

    CallRecord callRecordHead;
    final Object callRecordLock = new Object();

    CallRecord nextRecord(HybridMethod hybridMethod) {
        synchronized (callRecordLock) {
            CallRecord tmp = callRecordHead;
            CallRecord prev = null;
            while (tmp != null) {
                if (tmp.hybridMethod.getMethodName().equals(hybridMethod.getMethodName())) {
                    break;
                }
                prev = tmp;
                tmp = tmp.next;
            }
            CallRecord callRecord = new CallRecord();
            callRecord.hybridMethod = hybridMethod;
            if (prev != null) {
                callRecord.index = prev.index + 1;
            } else {
                callRecord.index = 0;
            }
            return callRecord;
        }
    }

    void addRecord(CallRecord callRecord) {
        synchronized (callRecordLock) {
            CallRecord tmp = callRecordHead;
            CallRecord prev = null;
            while (tmp != null) {
                if (tmp.hybridMethod.getMethodName().equals(callRecord.hybridMethod.getMethodName())
                        && tmp.index == callRecord.index) {
                    break;
                }
                prev = tmp;
                tmp = tmp.next;
            }
            if (prev != null) {
                prev.next = callRecord;
                callRecord.next = tmp;
            } else {
                callRecordHead = callRecord;
            }
        }
    }

    void removeCallRecord(String methodName, int index) {
        synchronized (callRecordLock) {
            CallRecord tmp = callRecordHead;
            CallRecord prev = null;
            while (tmp != null) {
                if (tmp.hybridMethod.getMethodName().equals(methodName)
                        && tmp.index == index) {
                    if (prev == null) {
                        callRecordHead = tmp.next;
                    } else {
                        prev.next = tmp.next;
                    }
                    break;
                }
                prev = tmp;
                tmp = tmp.next;
            }
        }
    }

    void dispatchJSCall(String methodName, int index, String arg) {
        synchronized (callRecordLock) {
            CallRecord tmp = callRecordHead;
            while (tmp != null) {
                if (tmp.hybridMethod.getMethodName().equals(methodName)
                        && tmp.index == index) {
                    tmp.hybridMethod.dispatchJSCall(arg);
                    return;
                }
                tmp = tmp.next;
            }
            WebLogger.error("cannot find target method with name :" + methodName);
        }
    }

    static class CallContext {
        CallRecord callRecord;
        String callbackJS;
    }

    void clearRecords() {
        synchronized (callRecordLock) {
            callRecordHead = null;
        }
    }
}

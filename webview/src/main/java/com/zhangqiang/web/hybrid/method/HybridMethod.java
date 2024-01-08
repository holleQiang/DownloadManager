package com.zhangqiang.web.hybrid.method;

import androidx.annotation.NonNull;

public abstract class HybridMethod {

    private final String methodName;

    public HybridMethod(String methodName) {
        this.methodName = methodName;
    }

    public final String getMethodName() {
        return methodName;
    }

    public void dispatchCallback(String arg) {
        onCallback(arg);
    }

    protected abstract void onCallback(String arg);

    public  String buildJavascript(CallbackJavascriptBuilder callbackJavascript){
        return onBuildJavascript(callbackJavascript);
    }

    protected abstract String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder);

    @NonNull
    @Override
    public String toString() {
        return "HybridMethod: "+getMethodName();
    }
}

package com.zhangqiang.web.hybrid;

public abstract class HybridMethod {

    public abstract String getMethodName();

    public void dispatchJSCall(String arg) {
        onJSCall(arg);
    }

    protected abstract void onJSCall(String arg);

    public abstract String buildInvokeJS(InvokeJSBuilder builder);
}

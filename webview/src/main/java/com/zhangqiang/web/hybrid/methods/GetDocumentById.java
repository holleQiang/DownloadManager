package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;

public class GetDocumentById extends HybridMethod {

    public GetDocumentById(String methodName) {
        super(methodName);
    }

    @Override
    protected void onCallback(String arg) {

    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "";
    }
}

package com.zhangqiang.web.hybrid;

public class QueryElementByTagMethod  extends HybridMethod{
    @Override
    public String getMethodName() {
        return "query_element_by_tag";
    }

    @Override
    protected void onJSCall(String arg) {

    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder callbackJSBuilder) {
        return null;
    }
}

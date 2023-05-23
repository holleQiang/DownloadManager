package com.zhangqiang.web.hybrid;

public class RemoveElementByIDMethod extends HybridMethod{

    private final String id;

    public RemoveElementByIDMethod(String id) {
        this.id = id;
    }

    @Override
    public String getMethodName() {
        return "remove_element_by_id";
    }

    @Override
    protected void onJSCall(String arg) {

    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder callbackJSBuilder) {
        return "(function (){\n" +
                "   const element = document.getElementById('" + id + "');\n" +
                "   if(element && element.parentElement){\n"+
                "      element.parentElement.removeChild(element);\n"+
                "   }\n"+
                "})()\n";
    }
}

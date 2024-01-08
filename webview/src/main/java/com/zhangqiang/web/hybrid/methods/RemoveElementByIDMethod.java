package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;

public class RemoveElementByIDMethod extends HybridMethod {

    private final String id;

    public RemoveElementByIDMethod(String id) {
        super( "remove_element_by_id");
        this.id = id;
    }

    @Override
    protected void onCallback(String arg) {

    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "(function (){\n" +
                "   const element = document.getElementById('" + id + "');\n" +
                "   if(element && element.parentElement){\n"+
                "      element.parentElement.removeChild(element);\n"+
                "   }\n"+
                "})()\n";
    }
}

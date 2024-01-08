package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.log.WebLogger;

public class RemoveElementByTagMethod extends HybridMethod {

    private static final String METHOD_NAME = "remove_element_by_tag";
    private final String tagName;
    private final RemoveResultCallback callback;

    public RemoveElementByTagMethod(String tagName) {
        this(tagName, new RemoveResultCallback() {
            @Override
            public void onRemoveResult(int count) {
                WebLogger.info("移除了"+count+"个"+tagName+"标签");
            }
        });
    }

    public RemoveElementByTagMethod(String tagName, RemoveResultCallback callback) {
        super(METHOD_NAME);
        this.tagName = tagName;
        this.callback = callback;
    }

    @Override
    protected void onCallback(String arg) {
        if (callback == null) {
            return;
        }
        callback.onRemoveResult(Integer.parseInt(arg));
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "(function (){\n" +
                "   const elements = document.getElementsByTagName('" + tagName + "');\n" +
                "   let count = 0;\n" +
                "   if(!elements){\n" +
                callbackJavascriptBuilder.buildCallbackJavascript("count", "   ") +
                "       return;\n" +
                "   };\n" +

                "   for(let i =0;i < elements.length;i++){\n" +
                "      const element = elements[i];\n" +
                "       if(element && element.parentElement){\n" +
                "           element.parentElement.removeChild(element);\n" +
                "           count++;\n" +
                "       };\n" +
                "   };\n" +
                callbackJavascriptBuilder.buildCallbackJavascript("count", "   ") +
                "})();\n";
    }

    public interface RemoveResultCallback{

        void onRemoveResult(int count);
    }
}

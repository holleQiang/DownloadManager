package com.zhangqiang.web.hybrid;

public class RemoveElementByTagMethod extends HybridMethod {

    private static final String METHOD_NAME = "remove_element_by_tag";
    private final String tagName;

    public RemoveElementByTagMethod(String tagName) {
        this.tagName = tagName;
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    protected void onJSCall(String arg) {

    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder builder) {
        return "(function (){\n" +
                "   const elements = document.getElementsByTagName('" + tagName + "');\n" +
                "   if(!elements){\n" +
                "       return;\n" +
                "   };\n" +
                "   for(let i =0;i < elements.length;i++){\n" +
                "      const element = elements[i];\n" +
                "       if(element && element.parentElement){\n" +
                "           element.parentElement.removeChild(element);\n" +
                "       };\n" +
                "   };\n" +
                "})();\n";
    }
}

package com.zhangqiang.web.hybrid;

public class DOMChangeMonitorMethod extends HybridMethod {

    private final OnDOMChangedListener onDOMChangedListener;

    public DOMChangeMonitorMethod(OnDOMChangedListener onDOMChangedListener) {
        this.onDOMChangedListener = onDOMChangedListener;
    }

    @Override
    public String getMethodName() {
        return "dom_change_monitor";
    }

    @Override
    protected void onJSCall(String arg) {
        onDOMChangedListener.onDOMChanged();
    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder callbackJSBuilder) {
        return "(function(){\n" +
                "   const observer = new MutationObserver(function (mutationList, observer){\n" +
                "       const length = mutationList.length;\n" +
                "       for(let i = 0;i < length;i ++){\n" +
                "           const record = mutationList[i];\n" +
                "           console.log(`--record---type:${record.type}---target:${record.target}---attributeName:${record.attributeName}`);\n" +
                "       };\n" +
                callbackJSBuilder.buildCallbackJS(null,
                        new CallbackJSBuilder.Options().setLinePrefix("      ")) +
                "   });\n" +
                "   const options = {\n" +
                "       childList:true,\n" +
                "       subtree:true,\n" +
                "       attributes:true,\n" +
                "       attributeFilter:['id']\n" +
                "   };\n" +
                "   observer.observe(document.documentElement,options);\n" +
                "})();";
    }

    public interface OnDOMChangedListener {
        void onDOMChanged();
    }
}

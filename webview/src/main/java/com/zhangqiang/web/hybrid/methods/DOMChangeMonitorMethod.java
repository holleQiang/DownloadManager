package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;

public class DOMChangeMonitorMethod extends HybridMethod {

    private final OnDOMChangedListener onDOMChangedListener;

    public DOMChangeMonitorMethod(OnDOMChangedListener onDOMChangedListener) {
        super("dom_change_monitor");
        this.onDOMChangedListener = onDOMChangedListener;
    }

    @Override
    protected void onCallback(String arg) {
        onDOMChangedListener.onDOMChanged();
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "(function(){\n" +
                "   const observer = new MutationObserver(function (mutationList, observer){\n" +
                "       const length = mutationList.length;\n" +
                "       for(let i = 0;i < length;i ++){\n" +
                "           const record = mutationList[i];\n" +
                "           console.log(`--record---type:${record.type}---target:${record.target}---attributeName:${record.attributeName}`);\n" +
                "       };\n" +
                callbackJavascriptBuilder.buildCallbackJavascript(null, "       ") +
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

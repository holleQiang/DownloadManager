package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;

public class DocumentLoadMonitorMethod extends HybridMethod {

    private static final String METHOD_NAME = "document_load_monitor";
    private final OnDocumentLoadedListener onDocumentLoadedListener;

    public DocumentLoadMonitorMethod(OnDocumentLoadedListener onDocumentLoadedListener) {
        super(METHOD_NAME);
        this.onDocumentLoadedListener = onDocumentLoadedListener;
    }

    @Override
    protected void onCallback(String arg) {
        onDocumentLoadedListener.onDocumentLoaded();
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "(function (){\n" +
                "   document.addEventListener('DOMContentLoaded',function(){\n" +
                "         console.log('-----------DOMContentLoaded------------');\n" +
                callbackJavascriptBuilder.buildCallbackJavascript(null, "         ") +
                "   });\n" +
                "})()";
    }


    public interface OnDocumentLoadedListener {
        void onDocumentLoaded();
    }
}

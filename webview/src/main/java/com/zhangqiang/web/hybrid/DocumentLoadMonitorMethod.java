package com.zhangqiang.web.hybrid;

public class DocumentLoadMonitorMethod extends HybridMethod {

    private static final String METHOD_NAME = "document_load_monitor";
    private final OnDocumentLoadedListener onDocumentLoadedListener;

    public DocumentLoadMonitorMethod(OnDocumentLoadedListener onDocumentLoadedListener) {
        this.onDocumentLoadedListener = onDocumentLoadedListener;
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    protected void onJSCall(String arg) {
        onDocumentLoadedListener.onDocumentLoaded();
    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder builder) {
        CallbackJSBuilder.Options options = new CallbackJSBuilder.Options();
        options.setLinePrefix("         ");
        return "(function (){\n" +
                "   document.addEventListener('DOMContentLoaded',function(){\n" +
                "   console.log('-----------DOMContentLoaded------------');\n"+
                builder.buildCallbackJS(null, options) +
                "   });\n" +
                "})()";
    }

    public interface OnDocumentLoadedListener {
        void onDocumentLoaded();
    }
}

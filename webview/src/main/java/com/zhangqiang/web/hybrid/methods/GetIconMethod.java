package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.log.WebLogger;

public class GetIconMethod extends HybridMethod {

    public interface Callback {
        void onGetIconUrl(String iconUrl);
    }

    private Callback callback;

    public GetIconMethod setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public GetIconMethod() {
        super("getIcon");
    }

    @Override
    protected void onCallback(String arg) {
        if (callback != null) {
            callback.onGetIconUrl(arg);
        }
        WebLogger.info("---GetIconMethod-----------" + arg);
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        return "(function(){\n" +
                "   const links = document.getElementsByTagName('link');\n" +
                "   let iconUrl = '';\n" +
                "   for(const link of links){\n" +
                "       console.log('-------href---',link.href,'-----rel---',link.rel);\n" +
                "       if(link.rel === 'icon' " +
                "|| link.rel === 'icon shortcut' " +
                "|| link.rel === 'shortcut icon' " +
                "|| link.rel === 'apple-touch-icon'){\n" +
                "           iconUrl = link.href;\n" +
                "           break;\n" +
                "       };\n" +
                "   };\n" +
                callbackJavascriptBuilder.buildCallbackJavascript("iconUrl", "   ") +
                "})();\n";
    }
}

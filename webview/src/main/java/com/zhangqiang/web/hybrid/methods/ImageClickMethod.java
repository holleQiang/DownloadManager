package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.hybrid.CallbackJSBuilder;

public class ImageClickMethod extends HybridMethod {

    public static final String METHOD_NAME = "image_click";

    private final OnImageClickListener onImageClickListener;

    public ImageClickMethod(OnImageClickListener onImageClickListener) {
        super(METHOD_NAME);
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    protected void onCallback(String arg) {
        onImageClickListener.onImageClick(arg);
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
        //通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
        CallbackJSBuilder.Options options = new CallbackJSBuilder.Options();
        options.setLinePrefix("         ");
        return "(function(){\n" +
                "   const imgElements = document.getElementsByTagName(\"img\");\n" +
                "   for(var i=0;i<imgElements.length;i++){\n" +
                "       const imgElement = imgElements[i];\n" +
                "       imgElement.style['pointer-events']='auto';\n" +
                "       imgElement.onclick=function(){\n" +
                "       console.log(`---------${this.src}`);\n" +
                callbackJavascriptBuilder.buildCallbackJavascript("this.src", "       ") +
                "       }\n" +
                "   };\n" +
                "})();";
    }

    public interface OnImageClickListener {
        void onImageClick(String src);
    }
}

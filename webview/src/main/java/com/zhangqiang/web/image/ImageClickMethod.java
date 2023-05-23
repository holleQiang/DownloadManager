package com.zhangqiang.web.image;

import com.zhangqiang.web.hybrid.HybridMethod;
import com.zhangqiang.web.hybrid.CallbackJSBuilder;

public class ImageClickMethod extends HybridMethod {

    public static final String METHOD_NAME = "image_click";

    private final OnImageClickListener onImageClickListener;

    public ImageClickMethod(OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    @Override
    public String getMethodName() {
        return METHOD_NAME;
    }

    @Override
    protected void onJSCall(String arg) {
        onImageClickListener.onImageClick(arg);
    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder builder) {
        //通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
        CallbackJSBuilder.Options options = new CallbackJSBuilder.Options();
        options.setLinePrefix("         ");
        return "(function(){\n" +
                "   const imgElements = document.getElementsByTagName(\"img\");\n" +
                "   for(var i=0;i<imgElements.length;i++){\n" +
                "       const imgElement = imgElements[i];\n"+
                "       imgElement.style['pointer-events']='auto';\n"+
                "       imgElement.onclick=function(){\n" +
                "       console.log(`---------${this.src}`);\n"+
                builder.buildCallbackJS("this.src", options) +
                "       }\n" +
                "   };\n" +
                "})();";
    }

    public interface OnImageClickListener {
        void onImageClick(String src);
    }
}

package com.zhangqiang.web.hybrid.methods.selectElements;

import android.content.Context;

import com.zhangqiang.common.utils.AssetsUtils;
import com.zhangqiang.common.utils.JSONUtils;
import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.hybrid.methods.element.Element;
import com.zhangqiang.web.hybrid.methods.getElementsByTagName.GetElementsByTagName;
import com.zhangqiang.web.hybrid.methods.utils.ElementUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SelectElements extends HybridMethod {

    public interface ResultCallback {
        void onSuccess(List<Element> elements);

        void onFail(Throwable e);
    }

    private final Context context;
    private final String selector;
    private final GetElementsByTagName.ResultCallback callback;

    public SelectElements(Context context, String selector, GetElementsByTagName.ResultCallback callback) {
        super("selectElements");
        this.context = context;
        this.selector = selector;
        this.callback = callback;
    }

    @Override
    protected void onCallback(String arg) {
        try {
            JSONArray jsonArray = new JSONArray(arg);
            int length = jsonArray.length();
            List<Element> elements = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String tagName = jsonObject.optString("localName");
                elements.add(JSONUtils.toObject(jsonObject.toString(), ElementUtils.getElementClassByTagName(tagName)));
            }
            if (callback != null) {
                callback.onSuccess(elements);
            }
        } catch (JSONException e) {
            if (callback != null) {
                callback.onFail(e);
            }
        }
    }

    @Override
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {

        String jsString = AssetsUtils.readAsString(context, "selectElements.js");
        String finalJSString = jsString.replace("#{selector}", selector);

        return "(()=>{" +
                "const elements = " +finalJSString+";"+
                "const outputElements = [];"+
                "for(const element of elements){" +
                "   const keys = Object.keys(element);" +
                "   const outputElement = {};" +
                "   for(const key of keys){" +
                "       const value = element[key];" +
                "       if(typeof value === 'string' || typeof value === 'number'){" +
                "          outputElement[key] = value;" +
                "       }" +
                "   }" +
                "   outputElements.push(outputElement);" +
                "}"+
                "})();";
    }
}

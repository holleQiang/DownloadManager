package com.zhangqiang.web.hybrid.methods.getElementsByTagName;

import com.zhangqiang.common.utils.JSONUtils;
import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;
import com.zhangqiang.web.hybrid.methods.element.Element;
import com.zhangqiang.web.hybrid.methods.utils.ElementUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class GetElementsMethod extends HybridMethod {

    public interface ResultCallback {
        void onSuccess(List<Element> elements);

        void onFail(Throwable e);
    }

    private final ResultCallback callback;

    public GetElementsMethod(String methodName, ResultCallback callback) {
        super(methodName);
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
                elements.add(JSONUtils.toObject(jsonObject.toString(), ElementUtils.getElementClassByTagName("")));
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
        return "";
    }

    protected abstract String buildElementsGetJS();
}

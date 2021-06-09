package com.zhangqiang.sample.utils;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.util.TypedValue;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-09
 */
public class ResourceUtils {

    public static int getAttrColor(Context context,@AttrRes int colorAttr){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, typedValue, true);
        return typedValue.data;
    }
}

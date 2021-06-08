package com.zhangqiang.sample.utils;

import android.content.Context;
import android.util.TypedValue;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-08
 */
public class ScreenUtils {

    public static int dp2Px(Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics());
    }
}

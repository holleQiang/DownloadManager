package com.zhangqiang.web.export;

import com.zhangqiang.web.WebContext;

public class OnImageClickListeners extends BaseListeners<OnImageClickListener> {

    public void dispatchImageClick(WebContext webContext, String src) {
        doTraversal(new TraversalFunc<OnImageClickListener>() {
            @Override
            public void run(OnImageClickListener onImageClickListener) {
                onImageClickListener.onImageClick(webContext,src);
            }
        });
    }
}

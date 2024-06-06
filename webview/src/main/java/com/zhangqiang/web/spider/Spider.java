package com.zhangqiang.web.spider;

import com.zhangqiang.web.context.WebContext;

public abstract class Spider {

    private final WebContext webContext;
    private final String name;

    public Spider(WebContext webContext, String name) {
        this.webContext = webContext;
        this.name = name;
    }

    public interface OnFinishedListener{

    }

    public void start(){
        onStart();
    }

    protected abstract void onStart();

    public void cancel(){
        onCancel();
    }

    protected abstract void onCancel();

    public WebContext getWebContext() {
        return webContext;
    }
}

package com.zhangqiang.downloadmanager.task.interceptor;

import java.util.List;

public class RealCallChain implements Chain{

    private final List<Interceptor> interceptors;
    private final int index;

    public RealCallChain(List<Interceptor> interceptors, int index) {
        this.interceptors = interceptors;
        this.index = index;
    }

    @Override
    public void proceed() {
        if (index < 0 || index >= interceptors.size()) {
            throw new IndexOutOfBoundsException("illegal index");
        }
        RealCallChain nextChain = new RealCallChain(interceptors, index + 1);
        interceptors.get(index).onIntercept(nextChain);
    }
}

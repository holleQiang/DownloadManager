package com.zhangqiang.downloadmanager.task.interceptor.fail;

import java.util.List;

public class RealFailChain extends FailChain {

    private final List<FailInterceptor> failInterceptors;
    private final int index;

    public RealFailChain(Throwable e, List<FailInterceptor> failInterceptors, int index) {
        super(e);
        this.failInterceptors = failInterceptors;
        this.index = index;
    }

    @Override
    public void proceed(Throwable e) {
        if (index < 0 || index >= failInterceptors.size()) {
            throw new IndexOutOfBoundsException("illegal index");
        }
        RealFailChain next = new RealFailChain(e, failInterceptors, index + 1);
        FailInterceptor failInterceptor = failInterceptors.get(index);
        failInterceptor.onIntercept(next);
    }
}

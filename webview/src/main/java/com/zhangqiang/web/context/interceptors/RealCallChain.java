package com.zhangqiang.web.context.interceptors;

import java.util.List;

public class RealCallChain extends Chain {

    private final List<UrlLoadingInterceptor> interceptors;
    private final int index;

    public RealCallChain(String url, List<UrlLoadingInterceptor> interceptors, int index) {
        super(url);
        this.interceptors = interceptors;
        this.index = index;
    }

    @Override
    public boolean proceed(String url) {
        if (index < 0 || index >= interceptors.size()) {
            throw new IndexOutOfBoundsException("index out of bounds");
        }
        UrlLoadingInterceptor interceptor = interceptors.get(index);
        RealCallChain next = new RealCallChain(url, interceptors, index + 1);
        return interceptor.onInterceptUrlLoading(next);
    }
}

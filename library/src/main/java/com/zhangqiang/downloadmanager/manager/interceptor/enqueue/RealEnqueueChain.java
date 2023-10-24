package com.zhangqiang.downloadmanager.manager.interceptor.enqueue;

import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.downloadmanager.task.DownloadTask;

import java.util.List;

public class RealEnqueueChain extends Chain{

    private final List<EnqueueInterceptor> interceptors;
    private final int index;

    public RealEnqueueChain(DownloadRequest request, List<EnqueueInterceptor> interceptors, int index) {
        super(request);
        this.interceptors = interceptors;
        this.index = index;
    }

    @Override
    public DownloadTask proceed(DownloadRequest request) {
        if(index < 0 || index >= interceptors.size()){
            throw new IllegalArgumentException("illegal index"+index);
        }
        RealEnqueueChain next = new RealEnqueueChain(request, interceptors, index + 1);
        return interceptors.get(index).onIntercept(next);
    }
}

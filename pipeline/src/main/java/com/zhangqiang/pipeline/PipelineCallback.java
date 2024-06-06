package com.zhangqiang.pipeline;

public interface PipelineCallback {

    void onComplete();

    void onFail();
}

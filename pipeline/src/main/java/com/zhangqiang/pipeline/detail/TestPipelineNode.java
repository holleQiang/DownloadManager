package com.zhangqiang.pipeline.detail;

import android.os.Handler;
import android.os.Looper;

import com.zhangqiang.pipeline.node.PipelineNode;
import com.zhangqiang.pipeline.node.ResultCallback;

import java.util.List;

public class TestPipelineNode extends PipelineNode {

    public TestPipelineNode(String name,List<PipelineNode> childPipelineNodes, List<PipelineNode> requiredPipelineNodes) {
        super(name,childPipelineNodes, requiredPipelineNodes);
    }

    @Override
    protected void onExecute(ResultCallback callback) {
       new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
           @Override
           public void run() {
               callback.onSuccess();
           }
       },2000);
    }
}

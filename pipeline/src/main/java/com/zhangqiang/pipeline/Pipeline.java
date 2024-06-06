package com.zhangqiang.pipeline;

import com.zhangqiang.pipeline.node.PipelineNode;
import com.zhangqiang.pipeline.node.ResultCallback;
import com.zhangqiang.pipeline.node.State;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Pipeline {

    private final PipelineNode rootNode;

    public Pipeline(PipelineNode rootNode) {
        this.rootNode = rootNode;
    }

    public void execute() {

        performPipelineNodeExecute(rootNode);
    }

    private void performPipelineNodeExecute(PipelineNode pipelineNode){
        pipelineNode.execute(new ResultCallback() {
            @Override
            public void onSuccess() {
                List<PipelineNode> childPipelineNodes = pipelineNode.getChildPipelineNodes();
                if (childPipelineNodes == null || childPipelineNodes.isEmpty()) {
                    return;
                }
                for (PipelineNode childPipelineNode : childPipelineNodes) {
                    List<PipelineNode> requiredPipelineNodes = childPipelineNode.getRequiredPipelineNodes();
                    if (requiredPipelineNodes == null || requiredPipelineNodes.isEmpty()) {
                        throw new IllegalArgumentException("required pipeline node cannot be empty");
                    }
                    int successCount = 0;
                    for (PipelineNode requiredPipelineNode : requiredPipelineNodes) {
                        if (requiredPipelineNode.getState() == State.SUCCESS) {
                            successCount++;
                        }
                    }
                    if(successCount == requiredPipelineNodes.size()){
                        performPipelineNodeExecute(childPipelineNode);
                    }
                }
            }

            @Override
            public void onFail() {
                cancelAllRunningPipelineNode();
            }
        });
    }

    private void cancelAllRunningPipelineNode(){
        Deque<PipelineNode> pipelineNodes = new LinkedList<>();
        pipelineNodes.add(rootNode);

        while (!pipelineNodes.isEmpty()){
            PipelineNode pipelineNode = pipelineNodes.pollFirst();
            if (pipelineNode == null) {
                break;
            }
            if (pipelineNode.getState() == State.RUNNING) {
                pipelineNode.cancel();
            }
            List<PipelineNode> childPipelineNodes = pipelineNode.getChildPipelineNodes();
            if (childPipelineNodes == null) {
                continue;
            }
            for (PipelineNode childPipelineNode : childPipelineNodes) {
                pipelineNodes.offerLast(childPipelineNode);
            }
        }
    }
}

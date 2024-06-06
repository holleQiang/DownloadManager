package com.zhangqiang.pipeline.node;

import java.util.List;

public abstract class PipelineNode {

    private final String name;
    private final List<PipelineNode> childPipelineNodes;
    private final List<PipelineNode> requiredPipelineNodes;
    private State state = State.INITIAL;

    public PipelineNode(String name,List<PipelineNode> childPipelineNodes, List<PipelineNode> requiredPipelineNodes) {
        this.name = name;
        this.childPipelineNodes = childPipelineNodes;
        this.requiredPipelineNodes = requiredPipelineNodes;
    }

    public void execute(ResultCallback callback) {
        synchronized (this){
            if (state == State.RUNNING) {
                return;
            }
            state = State.RUNNING;
        }
        onExecute(new ResultCallback() {
            @Override
            public void onSuccess() {
                synchronized (PipelineNode.this){
                    if (state != State.RUNNING) {
                        return;
                    }
                    state = State.SUCCESS;
                }
                callback.onSuccess();
            }

            @Override
            public void onFail() {
                synchronized (PipelineNode.this){
                    if (state != State.RUNNING) {
                        return;
                    }
                    state = State.FAIL;
                }
                callback.onFail();
            }
        });
    }

    protected abstract void onExecute(ResultCallback callback);

    public void cancel(){
        synchronized (this){
            if (state != State.RUNNING) {
                return;
            }
            state = State.CANCELED;
        }
        onCancel();
    }

    private void onCancel() {

    }

    public List<PipelineNode> getChildPipelineNodes() {
        return childPipelineNodes;
    }

    public List<PipelineNode> getRequiredPipelineNodes() {
        return requiredPipelineNodes;
    }

    public State getState() {
        return state;
    }

    public String getName() {
        return name;
    }
}

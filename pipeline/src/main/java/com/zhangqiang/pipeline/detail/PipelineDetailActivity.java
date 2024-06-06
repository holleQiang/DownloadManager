package com.zhangqiang.pipeline.detail;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.pipeline.Pipeline;
import com.zhangqiang.pipeline.databinding.ActivityPipelineDetailBinding;
import com.zhangqiang.pipeline.detail.floor.PipelineNodeFloor;
import com.zhangqiang.pipeline.node.PipelineNode;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class PipelineDetailActivity extends BaseActivity {

    private ActivityPipelineDetailBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPipelineDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.rvPipelineDetail.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        CellRVAdapter adapter = new CellRVAdapter();
        binding.rvPipelineDetail.setAdapter(adapter);

        ArrayList<PipelineNode> childPipelineNodes = new ArrayList<>();
        PipelineNode rootNode = new TestPipelineNode("RootNode",childPipelineNodes,null);
        PipelineNode currentNode = rootNode;
        for (int i = 0; i < 10; i++) {
            ArrayList<PipelineNode> requiredPipelineNodes = new ArrayList<>();
            requiredPipelineNodes.add(currentNode);
            final PipelineNode required = currentNode;
            currentNode = new TestPipelineNode("LeafNode"+i,childPipelineNodes,requiredPipelineNodes);
            required.getChildPipelineNodes().add(currentNode);
        }
        Pipeline pipeline = new Pipeline(rootNode);

        binding.btExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pipeline.execute();
            }
        });

        List<PipelineNodeFloor> pipelineNodeFloors = new ArrayList<>();

        Deque<PipelineNode> queue = new LinkedList<>();
        queue.offerLast(rootNode);
        while (!queue.isEmpty()){
            PipelineNode pipelineNode = queue.pollFirst();
            if (pipelineNode == null) {
                throw new NullPointerException();
            }
            List<PipelineNode> childNodes = pipelineNode.getChildPipelineNodes();
            if (childNodes != null) {
                for (PipelineNode childNode : childNodes) {
                    queue.offerLast(childNode);
                }
            }
        }

        ArrayList<Cell> dataList = new ArrayList<>();
        adapter.setDataList(dataList);
    }

    private PipelineNodeFloor buildPipelineNodeFloor(PipelineNode pipelineNode){
        PipelineNodeFloor pipelineNodeFloor = new PipelineNodeFloor();
        ArrayList<PipelineNode> pipelineNodes = new ArrayList<>();
        pipelineNodes.add(pipelineNode);
        pipelineNodeFloor.setPipelineNodes(pipelineNodes);
        return pipelineNodeFloor;
    }
}

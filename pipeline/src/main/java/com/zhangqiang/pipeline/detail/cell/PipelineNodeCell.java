package com.zhangqiang.pipeline.detail.cell;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.common.utils.ListUtils;
import com.zhangqiang.pipeline.R;
import com.zhangqiang.pipeline.detail.floor.PipelineNodeFloor;
import com.zhangqiang.pipeline.node.PipelineNode;

import java.util.List;

public class PipelineNodeCell extends MultiCell<PipelineNodeFloor> {
    public PipelineNodeCell(int layoutId, PipelineNodeFloor data) {
        super(R.layout.item_pipeline_node, data, null);
    }

    @Override
    protected void onBindViewHolder(ViewHolder vh) {
        super.onBindViewHolder(vh);
        PipelineNodeFloor pipelineNodeFloor = getData();
        List<PipelineNode> pipelineNodes = pipelineNodeFloor.getPipelineNodes();
        String name = ListUtils.isEmpty(pipelineNodes) ? null : pipelineNodes.get(0).getName();
        vh.setText(R.id.tv_pipeline_name, name);
    }
}

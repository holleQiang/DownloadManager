package com.zhangqiang.sample.business.container;

import java.util.ArrayList;
import java.util.List;

public class ContainerProcessorManager {

    private static final ContainerProcessorManager instance = new ContainerProcessorManager();
    private final List<ContainerProcessor> containerProcessors = new ArrayList<>();
    public static ContainerProcessorManager getInstance(){
        return instance;
    }

    boolean dispatchActivityCreate(ContainerActivity activity){
        for (int i = 0; i < containerProcessors.size(); i++) {
            if (containerProcessors.get(i).processor(activity)) {
               return true;
            }
        }
        return false;
    }

    public void registerProcessor(ContainerProcessor processor){
        if (containerProcessors.contains(processor)) {
            return;
        }
        containerProcessors.add(processor);
    }

    public void unRegisterProcessor(ContainerProcessor processor){
        containerProcessors.remove(processor);
    }
}

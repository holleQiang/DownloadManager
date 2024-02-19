package com.zhangqiang.web.resource.collect;

import com.zhangqiang.web.resource.collect.bean.WebResource;

public interface OnResourceChangeListener {

    void  onLoadWebResource(String sessionId,WebResource webResource);
}

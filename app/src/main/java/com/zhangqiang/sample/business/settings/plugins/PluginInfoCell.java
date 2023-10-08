package com.zhangqiang.sample.business.settings.plugins;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager2.plugin.DownloadPlugin;
import com.zhangqiang.sample.R;

public class PluginInfoCell extends MultiCell<DownloadPlugin> {


    public PluginInfoCell( DownloadPlugin data) {
        super(R.layout.item_plugin_info, data, new ViewHolderBinder<DownloadPlugin>() {
            @Override
            public void onBind(ViewHolder viewHolder, DownloadPlugin plugin) {
                viewHolder.setText(R.id.tv_title,plugin.getName());
            }
        });
    }
}

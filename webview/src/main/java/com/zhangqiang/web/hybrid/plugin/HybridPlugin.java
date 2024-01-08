package com.zhangqiang.web.hybrid.plugin;

import android.graphics.Bitmap;
import android.webkit.WebView;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.PageLoadListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.hybrid.methods.ConsoleLogMonitorMethod;
import com.zhangqiang.web.hybrid.methods.DOMChangeMonitorMethod;
import com.zhangqiang.web.hybrid.methods.DocumentLoadMonitorMethod;
import com.zhangqiang.web.hybrid.methods.RemoveElementByIDMethod;
import com.zhangqiang.web.hybrid.methods.RemoveElementByTagMethod;
import com.zhangqiang.web.hybrid.methods.ImageClickMethod;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

import java.util.Arrays;
import java.util.List;

public class HybridPlugin implements WebPlugin {

    @Override
    public void apply(PluginContext context) {
        List<WebPlugin> plugins = context.findPlugins(new WebManager.Filter() {
            @Override
            public boolean onFilter(WebPlugin plugin) {
                return plugin instanceof JSCallPlugin;
            }
        });
        if (plugins == null || plugins.size() == 0) {
            return;
        }
        JSCallPlugin jsCallPlugin = (JSCallPlugin) plugins.get(0);
        context.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(State state, State oldState) {
                        if (state == State.WEB_VIEW_CREATE) {
                            webContext.addPageLoadListener(new PageLoadListener() {
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    jsCallPlugin.invokeHybridMethod(new ConsoleLogMonitorMethod(new ConsoleLogMonitorMethod.LogReceiver() {
                                        @Override
                                        public void onReceiveLog(String[] logs) {
                                            WebLogger.info("console.log@@@:"+ Arrays.asList(logs).toString());
                                        }
                                    }));
                                    jsCallPlugin.invokeHybridMethod(new DocumentLoadMonitorMethod(new DocumentLoadMonitorMethod.OnDocumentLoadedListener() {
                                        @Override
                                        public void onDocumentLoaded() {
                                            jsCallPlugin.invokeHybridMethod(new RemoveElementByTagMethod("sidjjad"));
                                            jsCallPlugin.invokeHybridMethod(new RemoveElementByIDMethod("ahsdow"));
                                        }
                                    }));
                                    jsCallPlugin.invokeHybridMethod(new DOMChangeMonitorMethod(new DOMChangeMonitorMethod.OnDOMChangedListener() {
                                        @Override
                                        public void onDOMChanged() {
                                            jsCallPlugin.invokeHybridMethod(new RemoveElementByTagMethod("sidjjad"));
                                            jsCallPlugin.invokeHybridMethod(new RemoveElementByIDMethod("ahsdow"));
                                        }
                                    }));
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    jsCallPlugin.invokeHybridMethod(new ImageClickMethod(new ImageClickMethod.OnImageClickListener() {
                                        @Override
                                        public void onImageClick(String src) {
                                            WebLogger.info("点击了图片:"+src);
                                        }
                                    }));
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}

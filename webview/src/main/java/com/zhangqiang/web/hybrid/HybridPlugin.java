package com.zhangqiang.web.hybrid;

import android.graphics.Bitmap;
import android.webkit.WebView;

import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.PageLoadListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.image.ImageClickMethod;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.manager.WebPlugin;

import java.util.Arrays;

public class HybridPlugin implements WebPlugin {

    public final JavaScriptInterface javaScriptInterface = new JavaScriptInterface();

    @Override
    public void apply(WebManager webManager) {
        webManager.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(State state, State oldState) {
                        if (state == State.WEB_VIEW_CREATE) {
                            javaScriptInterface.attachToWebContext(webContext);
                            webContext.addPageLoadListener(new PageLoadListener() {
                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    javaScriptInterface.call(new ConsoleLogMonitorMethod(new ConsoleLogMonitorMethod.LogReceiver() {
                                        @Override
                                        public void onReceiveLog(String[] logs) {
                                            WebLogger.info("console.log@@@:"+ Arrays.asList(logs).toString());
                                        }
                                    }));
                                    javaScriptInterface.call(new DocumentLoadMonitorMethod(new DocumentLoadMonitorMethod.OnDocumentLoadedListener() {
                                        @Override
                                        public void onDocumentLoaded() {
                                            javaScriptInterface.call(new RemoveElementByTagMethod("sidjjad"));
                                            javaScriptInterface.call(new RemoveElementByIDMethod("ahsdow"));
                                        }
                                    }));
                                    javaScriptInterface.call(new DOMChangeMonitorMethod(new DOMChangeMonitorMethod.OnDOMChangedListener() {
                                        @Override
                                        public void onDOMChanged() {
                                            javaScriptInterface.call(new RemoveElementByTagMethod("sidjjad"));
                                            javaScriptInterface.call(new RemoveElementByIDMethod("ahsdow"));
                                        }
                                    }));
                                }

                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    javaScriptInterface.call(new ImageClickMethod(new ImageClickMethod.OnImageClickListener() {
                                        @Override
                                        public void onImageClick(String src) {
                                        }
                                    }));
                                }
                            });
                        }else if(state == State.WEB_VIEW_DESTROY){
                            javaScriptInterface.detachFromWebContext();
                        }
                    }
                });
            }
        });
    }
}

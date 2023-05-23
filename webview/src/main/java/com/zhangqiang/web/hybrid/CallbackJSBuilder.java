package com.zhangqiang.web.hybrid;

public abstract class CallbackJSBuilder {

    public final String buildCallbackJS(String argDescriptor, Options options) {
        return onBuildCallbackJS(argDescriptor, options);
    }

    protected abstract String onBuildCallbackJS(String argDescriptor, Options options);

    public static class Options {

        private String linePrefix;

        public String getLinePrefix() {
            return linePrefix;
        }

        public Options setLinePrefix(String linePrefix) {
            this.linePrefix = linePrefix;
            return this;
        }
    }
}

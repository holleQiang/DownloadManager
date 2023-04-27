package com.zhangqiang.web.hybrid;

public abstract class InvokeJSBuilder {

    public abstract String buildInvokeJS(String argDescriptor,Options options);

    public static class Options{

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

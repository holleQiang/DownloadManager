package com.zhangqiang.web.hybrid;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConsoleLogMonitorMethod extends HybridMethod {

    private final LogReceiver logReceiver;

    public ConsoleLogMonitorMethod(LogReceiver logReceiver) {
        this.logReceiver = logReceiver;
    }

    @Override
    public String getMethodName() {
        return "console_log_monitor";
    }

    @Override
    protected void onJSCall(String arg) {
        try {
            JSONObject jsonObject = new JSONObject(arg);
            JSONArray jsonArray = jsonObject.optJSONArray("args");
            if (jsonArray == null) {
                return;
            }
            int length = jsonArray.length();
            String[] logs = new String[length];
            for (int i = 0; i < length; i++) {
                logs[i] = jsonArray.optString(i);
            }
            logReceiver.onReceiveLog(logs);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public String buildInvokeJS(CallbackJSBuilder callbackJSBuilder) {
        return "(function (){\n" +
                "   const orgLog = console.log;\n" +
                "   const newLog = function(...args){\n" +
                "       orgLog(args);\n" +
                "       const array = [];\n" +
                "       for(let i = 0;i < args.length;i++){\n" +
                "           const arg = args[i];\n" +
                "           array.push(arg);\n" +
                "       };\n" +
                "       const data = {};\n" +
                "       data.args = array;\n" +
                "       const dataStr = JSON.stringify(data);\n" +
                callbackJSBuilder.buildCallbackJS("dataStr",
                        new CallbackJSBuilder.Options().setLinePrefix("       ")) +
                "   }\n" +
                "   console.log = newLog;\n" +
                "})();\n";
    }

    public interface LogReceiver {
        void onReceiveLog(String[] logs);
    }
}

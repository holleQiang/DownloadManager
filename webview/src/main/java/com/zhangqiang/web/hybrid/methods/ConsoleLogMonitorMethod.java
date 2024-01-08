package com.zhangqiang.web.hybrid.methods;

import com.zhangqiang.web.hybrid.method.CallbackJavascriptBuilder;
import com.zhangqiang.web.hybrid.method.HybridMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConsoleLogMonitorMethod extends HybridMethod {

    private final LogReceiver logReceiver;

    public ConsoleLogMonitorMethod(LogReceiver logReceiver) {
        super("console_log_monitor");
        this.logReceiver = logReceiver;
    }

    @Override
    protected void onCallback(String arg) {
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
    protected String onBuildJavascript(CallbackJavascriptBuilder callbackJavascriptBuilder) {
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
                "       " + callbackJavascriptBuilder.buildCallbackJavascript("dataStr","       ") +
                "   }\n" +
                "   console.log = newLog;\n" +
                "})();\n";
    }

    public interface LogReceiver {
        void onReceiveLog(String[] logs);
    }
}

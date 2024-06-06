package com.zhangqiang.web.settings;


import android.content.Context;

import androidx.annotation.IntDef;

import com.zhangqiang.options.Option;
import com.zhangqiang.options.Options;
import com.zhangqiang.options.store.ValueStore;
import com.zhangqiang.options.store.shared.SharedValueStore;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class WebViewSettings {

    public static final String USER_AGENT_CHROME_MACOS = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    public static final String USER_AGENT_CHROME_WINDOWS = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";
    public static final String USER_AGENT_CHROME_ANDROID = "Mozilla/5.0 (Linux; Android 6.0.1; SOV33 Build/35.0.D.0.326) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.91 Mobile Safari/537.36";

    public static final String USER_AGENT_SAFARI_IOS = "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.0 Mobile/15E148 Safari/604.1";
    public static final String USER_AGENT_WX_MACOS = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/6.8.0(0x16080000) MacWechat/3.8.6(0x13080611) XWEB/1152 Flue";
    public static final String USER_AGENT_WX_WINDOWS = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36 NetType/WIFI MicroMessenger/7.0.20.1781(0x6700143B) WindowsWechat(0x63090819) XWEB/8519 Flue";
    public static final String USER_AGENT_WX_ANDROID = "Mozilla/5.0 (Linux; Android 13; 22081212C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36 XWEB/1160043 MMWEBSDK/20231105 MMWEBID/4478 MicroMessenger/8.0.44.2502(0x28002C51) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64";
    public static final String USER_AGENT_WX_LITE_PROGRAM = "Mozilla/5.0 (Linux; Android 13; 22081212C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36 XWEB/1160043 MMWEBSDK/20231105 MMWEBID/4478 MicroMessenger/8.0.44.2502(0x28002C51) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64 MiniProgramEnv/android";
    public static final String USER_AGENT_WX_LITE_PROGRAM_WEB_VIEW = "Mozilla/5.0 (Linux; Android 13; 22081212C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36 XWEB/1160043 MMWEBSDK/20231105 MMWEBID/4478 MicroMessenger/8.0.44.2502(0x28002C51) WeChat/arm64 Weixin NetType/WIFI Language/zh_CN ABI/arm64 miniProgram/wxdec51c0e2b06cbfe";
    public static final String USER_AGENT_ZFB_ANDROID = "Mozilla/5.0 (Linux; Android 13; 22081212C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/105.0.5195.148 MYWeb/0.11.0.231220162319 UWS/3.22.2.9999 UCBS/3.22.2.9999_220000000000 Mobile Safari/537.36 NebulaSDK/1.8.100112 Nebula AlipayDefined(nt:WIFI,ws:407";
    public static final String USER_AGENT_DY_ANDROID = "Mozilla/5.0 (Linux; Android 10; HD1900 Build/QKQ1.190716.003; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/75.0.3770.156 Mobile Safari/537.36 aweme_230400 JsSdk/1.0 NetType/WIFI AppName/aweme app_version/23.4.0 ByteLocale/zh-CN Region/CN AppSkin/white AppTheme/light BytedanceWebview/d8a21c6 WebView/075113004008";
    public static final String USER_AGENT_QQ_ANDROID = "Mozilla/5.0 (Linux; Android 13; 22081212C Build/TKQ1.220829.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/109.0.5414.86 MQQBrowser/6.2 TBS/046905 Mobile Safari/537.36 V1_AND_SQ_9.0.0_5282_YYB_D PA QQ/9.0.0.14110 NetType/WIFI WebP/0.3.0 AppId/537194356 Pixel/1220 StatusBarHeight/89 SimpleUISwitch/1 QQTheme/2971 StudyMode/0 CurrentMode/1 CurrentFontScale/1.0 GlobalDensityScale/1.0166667 AllowLandscape/false InMagicWin/0";
    public static final int USER_AGENT_TYPE_MOBILE = 0;
    public static final int USER_AGENT_TYPE_PC = 1;
    private final Option<Integer> userAgentOption;

    private static volatile WebViewSettings instance;

    private WebViewSettings(Context context) {
        ValueStore valueStore = new SharedValueStore(context, "web_view_settings");
        userAgentOption = Options.ofInt("user_agent_type", USER_AGENT_TYPE_MOBILE, valueStore);
    }

    public static WebViewSettings get(Context context) {
        if (instance == null) {
            synchronized (WebViewSettings.class) {
                if (instance == null) {
                    instance = new WebViewSettings(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({USER_AGENT_TYPE_MOBILE, USER_AGENT_TYPE_PC})
    @interface UserAgentType {
    }

    public @UserAgentType int getUserAgentType() {
        return userAgentOption.get();
    }

    public void setUserAgentType(@UserAgentType int userAgentType) {
        userAgentOption.set(userAgentType);
    }
}

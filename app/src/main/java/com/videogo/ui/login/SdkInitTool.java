package com.videogo.ui.login;

import android.app.Application;
import android.support.annotation.NonNull;

import com.videogo.EzvizApplication;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;

public class SdkInitTool {

    public static void initSdk(@NonNull Application application, @NonNull SdkInitParams sdkInitParams) {
//        TestParams.setUse(true);
        if (sdkInitParams.usingGlobalSDK) {
            // sdk日志开关，必须在initLib方法之前调用，正式发布时必须设置为false或者删除；否则本地会缓存大量的调试文件
            EZGlobalSDK.showSDKLog(true);
            // 设置是否支持P2P取流,详见api
            EZGlobalSDK.enableP2P(true);
            // APP_KEY请替换成自己申请的
            EZGlobalSDK.initLib(application, sdkInitParams.appKey);
        } else {
            // sdk日志开关，必须在initLib方法之前调用，正式发布时必须设置为false或者删除；否则本地会缓存大量的调试文件
            EZOpenSDK.showSDKLog(true);
            // 设置是否支持P2P取流,详见api
            EZOpenSDK.enableP2P(true);
            // APP_KEY请替换成自己申请的
            EZOpenSDK.initLib(application, sdkInitParams.appKey);
        }
        EZOpenSDK ezvizSDK = EzvizApplication.getOpenSDK();
        if (sdkInitParams.accessToken != null) {
            ezvizSDK.setAccessToken(sdkInitParams.accessToken);
        }
        if (sdkInitParams.openApiServer != null && sdkInitParams.openAuthApiServer != null) {
            ezvizSDK.setServerUrl(sdkInitParams.openApiServer, sdkInitParams.openAuthApiServer);
        }
    }

}

package com.videogo.ui.login;

import android.app.Application;

import androidx.annotation.NonNull;

import com.videogo.EzvizApplication;
import com.videogo.global.GlobalKit;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;

public class SdkInitTool {

    public static void initSdk(@NonNull Application application, @NonNull SdkInitParams sdkInitParams) {
//        TestParams.setUse(true);
        if (sdkInitParams.usingGlobalSDK) {
            // sdk日志开关，在initLib方法之前调用；正式发布时建议设置为false或者删除；
            EZGlobalSDK.showSDKLog(true);
            // sdk码流缓存开关，在initLib方法之前调用；正式发布时必须设置为false或者删除；否则本地会缓存大量的调试文件
            EZGlobalSDK.setDebugStreamEnable(true);
            // 设置是否支持P2P取流,详见api
            EZGlobalSDK.enableP2P(true);
            GlobalKit.getInstance().enableP2P = true;
            // APP_KEY请替换成自己申请的
            EZGlobalSDK.initLib(application, sdkInitParams.appKey);

            EZOpenSDK ezvizSDK = EzvizApplication.getOpenSDK();
            if (sdkInitParams.token != null) {
                ezvizSDK.setAccessToken(sdkInitParams.token);
            }
            if (ezvizSDK != null && sdkInitParams.openApiServer != null && sdkInitParams.openAuthApiServer != null) {
                ezvizSDK.setServerUrl(sdkInitParams.openApiServer, sdkInitParams.openAuthApiServer);
            }
        } else {
            /**
             * 注意：Demo兼容accessToken模式和tkToken小权限模式，所以需要通过.tk进行判断
             * 开发者App只能使用一种模式，EZOpenSDK.enableSDKWithTKToken直接设置true或false
             */
            boolean isEnableSDKWithTKToken = false;
            String token = sdkInitParams.token;
            if (token != null) {
                isEnableSDKWithTKToken = token.startsWith("tk.");
            }

            // sdk日志开关，在initLib方法之前调用；正式发布时建议设置为false或者删除；
            EZOpenSDK.showSDKLog(true);
            // sdk码流缓存开关，在initLib方法之前调用；正式发布时必须设置为false或者删除；否则本地会缓存大量的调试文件
            EZOpenSDK.setDebugStreamEnable(true);
            // 设置是否支持P2P取流,详见api
            EZOpenSDK.enableP2P(true);
            GlobalKit.getInstance().enableP2P = true;
            // 设置是否支持小权限token，必须在initLib前调用
            EZOpenSDK.enableSDKWithTKToken(isEnableSDKWithTKToken);
            // APP_KEY请替换成自己申请的
            EZOpenSDK.initLib(application, sdkInitParams.appKey);

            EZOpenSDK ezvizSDK = EzvizApplication.getOpenSDK();
            if (token != null) {
                if (isEnableSDKWithTKToken) {
                    ezvizSDK.setHttpToken(token);
                } else {
                    ezvizSDK.setAccessToken(token);
                }
            }
            if (ezvizSDK != null && sdkInitParams.openApiServer != null && sdkInitParams.openAuthApiServer != null) {
                ezvizSDK.setServerUrl(sdkInitParams.openApiServer, sdkInitParams.openAuthApiServer);
            }
        }
    }

}

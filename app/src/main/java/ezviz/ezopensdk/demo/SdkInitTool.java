package ezviz.ezopensdk.demo;

import android.app.Application;
import android.support.annotation.NonNull;

import com.videogo.EzvizApplication;
import com.videogo.debug.TestParams;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;

public class SdkInitTool {

    public static void initSdk(@NonNull Application application, @NonNull SdkInitParams sdkInitParams) {
        TestParams.setUse(true);
        if (sdkInitParams.usingGlobalSDK) {
            // sdk日志开关，正式发布需要去掉
            EZGlobalSDK.showSDKLog(true);
            // 设置是否支持P2P取流,详见api
            EZGlobalSDK.enableP2P(true);
            // APP_KEY请替换成自己申请的
            EZGlobalSDK.initLib(application, sdkInitParams.appKey);
        } else {
            // sdk日志开关，正式发布需要去掉
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
